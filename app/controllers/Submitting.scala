package controllers

import org.joda.time.DateTime
import models._
import Mappers.{enumFormMapping, idTypeFormMapping}
import models.Role.Author
import play.api.data.{Form, FormError}
import play.api.data.Forms.{ignored, list, mapping, nonEmptyText, number, text}
import play.api.data.Mapping
import play.api.i18n.Messages
import play.api.mvc.{Action, Call, Controller, MultipartFormData}
import play.api.templates.Html

case class SubmissionForm(
  paper: Paper,
  authors: List[Person]
)

object Submitting extends Controller {
  private val required = Messages("error.required")
  def paperMapping: Mapping[Paper] = mapping(
    "title" -> nonEmptyText,
    "format" -> enumFormMapping(PaperType),
    "keywords" -> nonEmptyText,
    "abstrct" -> nonEmptyText,
    "nAuthors" -> number.verifying(required, _ > 0),
    "fileId" -> ignored(Option.empty[Id[File]]),
    "withdrawn" -> ignored(false),
    "metadata" -> ignored(newMetadata[Paper])
  )(Paper.apply _)(Paper.unapply _)
  
  def authorMapping: Mapping[Person] = mapping(
    "firstname" -> text,
    "lastname" -> text,
    "organization" -> text,
    "email" -> text,
    "metadata" -> ignored(newMetadata[Person])
  )(Person.apply _)(Person.unapply _)

  def submissionForm: Form[SubmissionForm] = Form(
    mapping(
      "paper" -> paperMapping,
      "authors" -> list(authorMapping)
    )(SubmissionForm.apply _)(SubmissionForm.unapply _)
  )
  
  private def currentTime(): DateTime = new DateTime()
  
  def submit = SlickAction(IsAuthor, _ => true) { implicit r =>
    if(Query(r.db).configuration.authorNewSubmission) {
      Ok(views.html.submissionform("New Submission", submissionForm, routes.Submitting.doSubmit, Navbar(Author)))
    } else {
      Ok(views.html.main("Submissions closed", Navbar(Author))(Html("This conference is not accepting new submissions.")))
    }
  }
  
  def doSubmit = SlickAction(IsAuthor, _.authorNewSubmission, parse.multipartFormData) { implicit r =>
    doSaveImpl(None, routes.Submitting.doSubmit, routes.Submitting.info, true)
  }
  
  def info(paperId: Id[Paper]) = SlickAction(IsAuthorOf(paperId), _ => true) { 
    implicit r =>
    val paper: Paper = Query(r.db).paperWithId(paperId)
    val canEdit: Boolean = Query(r.db).configuration.authorEditSubmission
    infoImpl(paperId,
      if(canEdit) Some(routes.Submitting.edit(paperId)) else None,
      if(canEdit && !paper.withdrawn) Some(routes.Submitting.withdraw(paperId)) else None,
      Navbar(Author))
  }

  def infoImpl(paperId: Id[Paper], optionalEditEP: Option[Call], optionalWithdrawEP: Option[Call], navbar: Html)(implicit r: SlickRequest[_]) = {
    Ok(views.html.submissioninfo("Submission " + Query(r.db).indexOf(paperId), Query(r.db).paperWithId(paperId), optionalEditEP, optionalWithdrawEP, navbar)(summaryImpl(paperId)))
  }

  def summaryImpl(paperId: Id[Paper])(implicit r: SlickRequest[_]): Html = {
    val paper = Query(r.db) paperWithId paperId
    views.html.submissionsummary(paper, Query(r.db) authorsOf paperId, paper.fileId.map(Query(r.db) fileWithId _))
  }

  def edit(paperId: Id[Paper]) = SlickAction(IsAuthorOfNotWithdrawn(paperId), _.authorEditSubmission) { 
    implicit r =>
    val form = submissionForm.fill(SubmissionForm(Query(r.db) paperWithId paperId, Query(r.db) authorsOf paperId))
    Ok(views.html.submissionform("Editing Submission " + Query(r.db).indexOf(paperId), form, routes.Submitting.doEdit(paperId), Navbar(Author)))
  }
    
  def doEdit(paperId: Id[Paper]) = SlickAction(IsAuthorOfNotWithdrawn(paperId), _.authorEditSubmission, parse.multipartFormData) { implicit r =>
    doSaveImpl(Some(paperId), routes.Submitting.doEdit(paperId), routes.Submitting.info, true)
  }

  def withdraw(paperId: Id[Paper]) = SlickAction(IsAuthorOf(paperId), _.authorEditSubmission) { 
    implicit r =>
    r.connection insert Query(r.db).paperWithId(paperId).copy(withdrawn=true)
    Redirect(routes.Submitting.info(paperId)) flashing Msg.author.withdrawn
  }
  
  private type Req = SlickRequest[MultipartFormData[play.api.libs.Files.TemporaryFile]]
  def doSaveImpl(optionalPaperId: Option[Id[Paper]], errorEP: Call, okEP: Id[Paper] => Call, checkSelfAuthor: Boolean)(implicit r: Req) = {
    // TODO: if the form is not js validated we might want to save the
    // uploaded file in case of errors. Otherwise the user will have to
    // select it again.
    val bindedForm = submissionForm.bindFromRequest
    
    val customErrors = bindedForm.fold(
      errors => Nil,
      form => {
        val authorsWIndex = form.authors.take(form.paper.nAuthors).zipWithIndex
        val emptyFieldErr: Seq[FormError] = authorsWIndex.flatMap { 
          case (Person(fn, ln, org, email, _), i) =>
            // Author fields are populated for author i; i < nAuthors
            Seq((fn, i, "firstname"), (ln, i, "lastname"), (org, i, "organization"), (email, i, "email"))
              .filter { _._1.trim().isEmpty }
              .map { case (_, i, field) => FormError(s"authors[$i]." + field, required) }
        }
        val sameEmailErr: Seq[FormError] = authorsWIndex.groupBy(_._1.email).map(_._2).flatMap { as =>
          if(as.length == 1) Seq()
          else as map { case (_, i) => 
            FormError(s"authors[$i].email", "Authors must have different emails")
          }
        }.toSeq
        val notAuthorErr: Seq[FormError] = {
          if(authorsWIndex.map(_._1.email).exists(_ == r.user.email)) Seq()
          else authorsWIndex.map { case (_, i) =>
            FormError(s"authors[$i].email", "The person submitting must be an author")
          }
        }
        
        emptyFieldErr ++ sameEmailErr ++ (if(checkSelfAuthor) notAuthorErr else Seq())
      }
    )

    bindedForm.copy(errors = bindedForm.errors ++ customErrors).fold(
      errors => Ok(views.html.submissionform("Submission: Errors found", errors, errorEP, Navbar(Author))(
        Msg.flash(if(optionalPaperId.isEmpty) Msg.author.submitError else Msg.author.editError)
      )),
      form => {
        val file: Option[File] = r.body.file("data") map { f =>
          val blob = scalax.io.Resource.fromFile(f.ref.file).byteArray
          File(f.filename, blob.size, blob)
        }
        val paper: Paper = optionalPaperId.map(id => form.paper.copy(metadata=withId(id))).getOrElse(form.paper).copy(fileId=file.map(_.id))
        val persons: List[Person] = form.authors.take(form.paper.nAuthors)
        val authors: List[PaperAuthor] = persons.zipWithIndex.map { pi =>
          PaperAuthor(paper.id, pi._1.id, pi._2)
        }
        val pindex = PaperIndex(paper.id)
        r.connection insert pindex
        r.connection insert paper
        r.connection insert file.toList
        r.connection insert persons
        r.connection insert authors
        
        if(optionalPaperId.isEmpty) {
          Mailer.send(Email(
            persons map (_.email) mkString ", ",
            Msg.subject.submitted,
            Msg.email.submitted(routes.Submitting.info(paper.id).absoluteURL())
          ))
        }
                
        Redirect(okEP(paper.id))
          .flashing(if(optionalPaperId.isEmpty) Msg.author.submited else Msg.author.edited)
      }
    )
  }
  
  def disabled = SlickAction(IsAuthor, _ => true) { implicit r =>
    Ok(views.html.main("Disabled", Navbar.empty)(Html("This page is disabled in the current phase of the conference.")))
  }
}
