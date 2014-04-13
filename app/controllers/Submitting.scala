package controllers

import models._
import models.PersonRole._
import models.PaperType._
import org.joda.time.{ DateTime, Seconds }
import play.api.templates.Html
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.mvc.{ Controller, Cookie }
import securesocial.core.{ SecureSocial, SecuredRequest }
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import play.api.db.slick.DB

case class SubmissionForm(
  paper: Paper,
  authors: List[Person],
  topics: List[Long]
)

object Submitting extends Controller with SecureSocial {
  val PaperTypeMapping: Mapping[PaperType] = mapping(
    "value" -> nonEmptyText)(PaperType.withName(_))(Some(_).map(_.toString))
  
  def paperMapping(email: String, now: DateTime): Mapping[Paper] = mapping(
    "metadata" -> ignored((ignoredId[Paper], now, email)),
    "title" -> nonEmptyText,
    "format" -> PaperTypeMapping,
    "keywords" -> nonEmptyText,
    "abstrct" -> nonEmptyText,
    "nauthors" -> number,
    "fileid" -> ignored(Option.empty[Id[File]])
  )(Paper.apply _)(Paper.unapply _)
  
  def authorMapping(email: String, now: DateTime): Mapping[Person] = mapping(
    "metadata" -> ignored((ignoredId[Person], now, email)),
    "firstname" -> nonEmptyText,
    "lastname" -> nonEmptyText,
    "organization" -> optional(nonEmptyText),
    "role" -> ignored(Submitter),
    "email" -> nonEmptyText
  )(Person.apply _)(Person.unapply _)
    
  def submissionForm(email: String, now: DateTime): Form[SubmissionForm] = Form(
    mapping(
      "paper" -> paperMapping(email, now),
      "authors" -> list(authorMapping(email, now)),
      "topics" -> list(longNumber).verifying("Please select at least one topic.", _.nonEmpty)
    )(SubmissionForm.apply _)(SubmissionForm.unapply _)
  )
  
  private def email(implicit request: SecuredRequest[_]): String = request.user.email.get
  
  /** Displays new submissions form. */
  def make = SecuredAction { implicit request =>
    DB withSession { implicit session =>
      Ok(views.html.submissiontemplate("New Submission", submissionForm(email, DateTime.now), Some(email), Topics.all, routes.Submitting.domake)(Html("")))
    }
  }
  
  /** Handles a new submission. Creates a database entry with the form data. */
  def domake = SecuredAction(parse.multipartFormData) { implicit request =>
    DB withSession { implicit session =>
      val now: DateTime = DateTime.now
      // TODO: if the form is not js validated we might want to save the
      // uploaded file in case of errors. Otherwise the user will have to
      // select it again.
      submissionForm(email, now).bindFromRequest.fold(
        errors => Ok(views.html.submissiontemplate(email + " Submission: Errors found " + errors, errors, Some(email), Topics.all, routes.Submitting.domake)(Html(""))),
        form => {
          val fileid: Option[Id[File]] = request.body.file("data").map{ file =>
            val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
            Files.ins(File((ignoredId, now, email), file.filename, blob.size, blob))
          }
          val paperId: Id[Paper] = Papers.ins(form.paper.copy(fileid=fileid))
          val personsId: List[Id[Person]] = Persons.saveAll(form.authors.take(form.paper.nauthors))
          Authors.insAll(personsId.zipWithIndex.map(
            pi => Author((ignoredId, now, email), paperId, pi._1, pi._2)))
          PaperTopics.insAll(form.topics.map(
            i => PaperTopic((ignoredId, now, email), paperId, Id(i))))
          Redirect(routes.Submitting.info(paperId.value))
        }
      )
    }
  }
  
  /** Displays the informations of a given submission. */
  def info(id: Long) = SecuredAction { implicit request =>
    DB withSession { implicit session =>
      // TODO: Check that request.user.email.get is chair or author...
      val paper: Paper = Papers.withId(Id[Paper](id))
      Ok(views.html.submissioninfo(paper, Authors.of(paper), Topics.of(paper), Some(email)))
    }
  }
  
  /** Displays the form to edit the informations of a given submission. */
  def edit(id: Long) = SecuredAction { implicit request =>
    DB withSession { implicit session =>
      // TODO: Check that request.user.email.get is chair or author...
      val paper: Paper = Papers.withId(Id[Paper](id))

      def incBind[T](form: Form[T], data: Map[String, String]) = form.bind(form.data ++ data)
      val existingSubmissionForm = incBind(
        submissionForm(email, DateTime.now).fill(SubmissionForm(paper, Authors.of(paper), List())),
        Topics.of(paper).map(topic => (s"topics[${topic.id.value}]", topic.id.value.toString)).toMap
      )
      
      Ok(views.html.submissiontemplate("Edit Submission", existingSubmissionForm, Some(email), Topics.all, routes.Submitting.doedit(id))(Html("")))
    }
  }
  
  /** Handles edit of a submission. Update the database entry with the form data. */
  def doedit(id: Long) = SecuredAction(parse.multipartFormData) { implicit request =>
    DB withSession { implicit session =>
      val now: DateTime = DateTime.now
      // TODO: Check that request.user.email.get is chair or author...
      val paperId: Id[Paper] = Id[Paper](id)
      submissionForm(email, DateTime.now).bindFromRequest.fold(
        errors => Ok(views.html.submissiontemplate(email + " Submission: Errors found " + errors, errors, Some(email), Topics.all, routes.Submitting.doedit(id))(Html(""))),
        form => {
          // TODO: DRY with domake
          val fileid: Option[Id[File]] = request.body.file("data").map{ file =>
            val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
            Files.ins(File((ignoredId, now, email), file.filename, blob.size, blob))
          }
          Papers.updt(form.paper.copy(metadata=(paperId, now, email), fileid=fileid))
          val personsId: List[Id[Person]] = Persons.saveAll(form.authors.take(form.paper.nauthors))
          Authors.insAll(personsId.zipWithIndex.map(pi => Author((ignoredId, now, email), paperId, pi._1, pi._2)))
          PaperTopics.insAll(form.topics.map(i => PaperTopic((ignoredId, now, email), paperId, Id(i))))
          Redirect(routes.Submitting.info(paperId.value))
        }
      )
    }
  }
}
