package controllers

import models._
import models.PersonRole.Submitter
import models.PaperType._
import org.joda.time.{ DateTime, Seconds }
import play.api.templates.Html
import play.api.data.{ Form, Mapping }
import play.api.data.Forms._
import play.api.mvc.{ Controller, Cookie, Call, MultipartFormData }
import play.api.Play.current
import play.api.db.slick.DB
import Utils._
import play.api.i18n.Messages
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import play.api.data.FormError

case class SubmissionForm(
  paper: Paper,
  authors: List[Person],
  topics: List[IdType]
)

object Submitting extends Controller {
  private val required = Messages("error.required")
  def paperMapping: Mapping[Paper] = mapping(
    "title" -> nonEmptyText,
    "format" -> enumMapping(PaperType),
    "keywords" -> nonEmptyText,
    "abstrct" -> nonEmptyText,
    "nauthors" -> number.verifying(required, _ > 0),
    "fileid" -> ignored(Option.empty[Id[File]]),
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
      "authors" -> list(authorMapping),
      "topics" -> list(Utils.idTypeMapping).verifying(required, _.nonEmpty)
    )(SubmissionForm.apply _)(SubmissionForm.unapply _)
  )
  
  private def currentTime(): DateTime = new DateTime()
  
  /** Displays new submissions form. */
  def make = SlickAction(IsSubmitter) { implicit r =>
    Ok(views.html.submissionform("New Submission", submissionForm, Query(r.db).allTopics, routes.Submitting.doMake, Navbar(Submitter)))
  }
  
  /** Displays the informations of a given submission. */
  def info(id: IdType) = SlickAction(IsAuthorOf(id)) { implicit r =>
    val paper: Paper = Query(r.db) paperWithId Id[Paper](id)
    Ok(views.html.main("Submission " + Query(r.db).indexOf(paper.id), Navbar(Submitter)) (
       views.html.submissioninfo(paper, Query(r.db).authorsOf(paper.id), Query(r.db).topicsOf(paper.id), paper.fileid.map(Query(r.db) fileWithId _))
    ))
  }
  
  /** Displays the form to edit the informations of a given submission. */
  def edit(id: IdType) = SlickAction(IsAuthorOf(id)) { implicit r =>
    val paper: Paper = Query(r.db) paperWithId Id[Paper](id)
    val allTopics: List[Topic] = Query(r.db).allTopics
    val paperTopics: List[Topic] = Query(r.db) topicsOf paper.id
    def incBind[T](form: Form[T], data: Map[String, String]) = form.bind(form.data ++ data)
    val existingSubmissionForm  = incBind(
      submissionForm.fill(SubmissionForm(paper, Query(r.db) authorsOf paper.id, Nil)),
      allTopics.zipWithIndex.filter(paperTopics contains _._1).map(ti =>
        (s"topics[${ti._2}]", ti._1.id.value.toString)).toMap
    )
    Ok(views.html.submissionform("Editing Submission " + Query(r.db).indexOf(paper.id), existingSubmissionForm, allTopics, routes.Submitting.doEdit(id), Navbar(Submitter)))
  }
  
  /** Handles a new submission. Creates a database entry with the form data. */
  def doMake = SlickAction(IsSubmitter, parse.multipartFormData) { implicit r =>
    doSave(None, routes.Submitting.doMake) // TODO: Use Option
  }
    
  /** Handles edit of a submission. Update the database entry with the form data. */
  def doEdit(id: IdType) = SlickAction(IsAuthorOf(id), parse.multipartFormData) { implicit r =>
    doSave(Some(Id[Paper](id)), routes.Submitting.doEdit(id))
  }
  
  private type Req = SlickRequest[MultipartFormData[play.api.libs.Files.TemporaryFile]]
  private def doSave(optionalPaperId: Option[Id[Paper]], errorEP: Call)(implicit r: Req) = {
    // TODO: if the form is not js validated we might want to save the
    // uploaded file in case of errors. Otherwise the user will have to
    // select it again.
    val bindedForm = submissionForm.bindFromRequest
    val authorsWIndex = bindedForm.get.authors.take(bindedForm.get.paper.nauthors).zipWithIndex
    val emptyFieldErr: Seq[FormError] = authorsWIndex.flatMap { 
      case (Person(fn, ln, org, email, _), i) =>
        // Author fields are populated for author i; i < nauthors
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
    val customErrors: Seq[FormError] = emptyFieldErr ++ sameEmailErr ++ notAuthorErr

    bindedForm.copy(errors = bindedForm.errors ++ customErrors).fold(
      errors => Ok(views.html.submissionform(
        "Submission: Errors found", errors, Query(r.db).allTopics, errorEP, Navbar(Submitter))),
      form => {
        val file: Option[File] = r.body.file("data") map { f =>
          val blob = scalax.io.Resource.fromFile(f.ref.file).byteArray
          File(f.filename, blob.size, blob)
        }
        val paper: Paper = optionalPaperId.map(form.paper.withId(_)).getOrElse(form.paper).copy(fileid=file map (_.id))
        val persons: List[Person] = form.authors.take(form.paper.nauthors)
        val authors: List[Author] = persons.zipWithIndex.map { pi =>
          Author(paper.id, pi._1.id, pi._2)
        }
        val paperTopics: List[PaperTopic] = form.topics.map { i =>
          PaperTopic(paper.id, Id[Topic](i))
        }
        val pindex = PaperIndex(paper.id)
        play.api.Logger.error(paper.toString)
        
        r.connection insert (pindex :: paper :: file.toList ::: persons ::: authors ::: paperTopics)
        Redirect(routes.Submitting.info(paper.id.value))
      }
    )
  }
}
