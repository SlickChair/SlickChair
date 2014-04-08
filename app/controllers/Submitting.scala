package controllers

import models._
import models.PersonRole._
import models.PaperType._
import org.joda.time.{DateTime, Seconds}
import play.api.templates.Html
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, nonEmptyText, number, optional, text}
import play.api.data.Mapping
import play.api.mvc.{Controller, Cookie}
import securesocial.core.SecureSocial
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import play.api.db.slick.DB

case class SubmissionForm(
  paper: Paper,
  authors: List[Person],
  topics: List[Int]
)

/** Semantically, Curse values need to be set when handling the POST on a
  * form before storing the Paper in the database... This could be made
  * typesafe with restricted case classes for forms and a mapping function */
object Curse {
  def apply[T]() = ignored(null.asInstanceOf[T])
}

object Submitting extends Controller with SecureSocial {
  val PaperTypeMapping: Mapping[PaperType] = mapping(
    "value" -> nonEmptyText)(PaperType.withName(_))(Some(_).map(_.toString))
  
  val paperMapping: Mapping[Paper] = mapping(
    "metadata" -> Curse[MetaData[Paper]],
    "title" -> nonEmptyText,
    "format" -> PaperTypeMapping,
    "keywords" -> nonEmptyText,
    "abstrct" -> nonEmptyText,
    "nauthors" -> number,
    "fileid" -> Curse[Id[File]]
  )(Paper.apply _)(Paper.unapply _)
  
  val authorMapping: Mapping[Person] = mapping(
    "metadata" -> Curse[MetaData[Person]],
    "firstname" -> nonEmptyText,
    "lastname" -> nonEmptyText,
    "organization" -> optional(nonEmptyText),
    "role" -> ignored(Submitter),
    "email" -> nonEmptyText
  )(Person.apply _)(Person.unapply _)
    
  val submissionForm: Form[SubmissionForm] = Form(
    mapping(
      "paper" -> paperMapping,
      "authors" -> list(authorMapping),
      "topics" -> list(number).verifying("Please select at least one topic.", _.nonEmpty)
    )(SubmissionForm.apply _)(SubmissionForm.unapply _)
  )
  
  def make = SecuredAction { implicit request =>
    DB withSession { implicit s: Session =>
      val email = request.user.email.get
      val page = Papers.withEmail(email) match {
        case None =>
          views.html.submissiontemplate("New Submission", submissionForm, Some(email), Topics.all) _
        case Some(paper) =>
          // TODO: Authors are not populated...
          def incBind[T](form: Form[T], data: Map[String, String]) = form.bind(form.data ++ data)
          val existingSubmissionForm = incBind(
            submissionForm.fill(SubmissionForm(paper, Authors.of(paper), List())),
            Topics.of(paper).map(topic => ("topics[%s]".format(topic.id), topic.id.toString)).toMap
          )
          views.html.submissiontemplate("Edit Submission", existingSubmissionForm, Some(email), Topics.all) _
        }
      val CookieName = "not-first-login"
      request.cookies.get(CookieName) match {
        case None =>
          val now = DateTime.now()
          val maxAge = Some(Seconds.secondsBetween(now, now.plusYears(1)).getSeconds())
          val modal = views.html.modal("First login", withCloseButton=true)(
            Html("Warnning, this is your first login in play")
          )
          Ok(page(modal)).withCookies(Cookie(CookieName, "", maxAge=maxAge))
        case Some(c) =>
          Ok(page(Html("")))
      }
    }
  }
  
  /** Handles submissions. Creates or update database entry with data of the form. */
  def domake = SecuredAction(parse.multipartFormData) { implicit request =>
    DB withSession { implicit s: Session =>
      val user = User.fromIdentity(request.user)
      val email = user.email
      submissionForm.bindFromRequest.fold(
        // TODO: if the form is not js validated we might want to save the
        //       uploaded file in case of errors. Otherwise the user will
        //       have to select it again.
        errors => Ok(views.html.submissiontemplate(email + "Submission: Errors found", errors, Some(email), Topics.all)(Html(""))),
        form => {
          // Ok(form.toString)  
          // val newFileId: Option[Int] = request.body.file("data").map{ file =>
          //   val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
          //   Files.ins(NewFile(file.filename, blob.size, DateTime.now, blob))
          // }
          
          // val paperId = Papers.withEmail(email) match {
          //   case None =>
          //     Papers.ins(NewPaper(
          //       contactemail = email,
          //       contactfirstname = request.user.firstName,
          //       contactlastname = request.user.lastName,
          //       submissiondate = DateTime.now,
          //       lastupdate = DateTime.now,
          //       accepted = None,
          //       form.paper.title,
          //       form.paper.format,
          //       form.paper.keywords,
          //       form.paper.abstrct,
          //       fileid = newFileId
          //     ))
          //   case Some(dbPaper) =>
          //     newFileId.map{ _ => dbPaper.fileid.map(i => Files.delete(i)) }
          //     Authors.deleteFor(dbPaper)
          //     PaperTopics.deleteFor(dbPaper)
          //     Papers.updt(form.paper.copy(
          //       id = dbPaper.id,
          //       contactemail = dbPaper.contactemail, // == email by construction
          //       submissiondate = dbPaper.submissiondate,
          //       lastupdate = DateTime.now,
          //       accepted = dbPaper.accepted,
          //       fileid = newFileId.orElse(dbPaper.fileid)
          //     ))
          //     dbPaper.id
          // }
          
          // Authors.insertAll(form.authors.map(_.copy(paperId)))
          // PaperTopics.insertAll(form.topics.map(PaperTopic(paperId, _)))
          // Redirect(routes.Submitting.info)
          Ok("")
        }
      )
    }
  }
  
  // TODO: Remove?
  def info = SecuredAction { implicit request =>
    DB withSession { implicit s: Session =>
      Papers.withEmail(request.user.email.get) match {
        case None => // TODO: Needed??
          Redirect(routes.Submitting.make)
        case Some(paper) =>
          Ok(views.html.submissioninfo(
            paper,
            Authors.of(paper),
            Topics.of(paper)
          ))
      }
    }
  }
}
