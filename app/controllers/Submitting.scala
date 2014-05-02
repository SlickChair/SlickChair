package controllers

import models._
import models.PersonRole.Submitter
import models.PaperType._
import org.joda.time.{ DateTime, Seconds }
import play.api.templates.Html
import play.api.data.{ Form, Mapping }
import play.api.data.Forms._
import play.api.mvc.{ Controller, Cookie }
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.mvc.Call
import play.api.mvc.MultipartFormData
import Utils._

case class SubmissionForm(
  paper: Paper,
  authors: List[Person],
  topics: List[IdType]
)

object Submitting extends Controller {
  /** Semantically, curse values need to be set when handling the POST on a
    * form before storing the Paper in the database... This could be made
    * type safe by using new case classes for forms. */
  private def curse[T] = ignored(null.asInstanceOf[T])

  val PaperTypeMapping: Mapping[PaperType] = mapping(
    "value" -> nonEmptyText)(PaperType.withName(_))(Some(_).map(_.toString))
  
  val paperMapping: Mapping[Paper] = mapping(
    "metadata" -> curse[MetaData[Paper]],
    "title" -> nonEmptyText,
    "format" -> PaperTypeMapping,
    "keywords" -> nonEmptyText,
    "abstrct" -> nonEmptyText,
    "nauthors" -> number,
    "fileid" -> curse[Option[Id[File]]]
  )(Paper.apply _)(Paper.unapply _)
  
  val authorMapping: Mapping[Person] = mapping(
    "metadata" -> curse[MetaData[Person]],
    "firstname" -> text,
    "lastname" -> text,
    "organization" -> text,
    "role" -> ignored(Submitter),
    "email" -> text
  )(Person.apply _)(Person.unapply _)

  // TODO Check that: 
  //      - all authors field are populated for author i, i < nauthors
  //      - authors have different emails
  //      - user is an author
  val submissionForm: Form[SubmissionForm] = Form(
    mapping(
      "paper" -> paperMapping,
      "authors" -> list(authorMapping),
      "topics" -> list(Utils.uuid).verifying("Please select at least one topic.", _.nonEmpty)
    )(SubmissionForm.apply _)(SubmissionForm.unapply _)
  )
  
  private def currentTime(): DateTime = new DateTime()
  
  /** Displays new submissions form. */
  def make = SlickAction(IsSubmitter) { implicit r =>
    Ok(views.html.submissiontemplate("New Submission", submissionForm, Topics.all, routes.Submitting.doMake, Navbar(r.user, Submitter))(Html("")))
  }
  
  /** Displays the informations of a given submission. */
  def info(id: IdType) = SlickAction(IsAuthorOf(id)) { implicit r =>
    val paper: Paper = Papers.withId(Id[Paper](id))
    Ok(views.html.submissioninfo(paper, Authors.of(paper.id), Topics.of(paper), Navbar(r.user, Submitter)))
  }
  
  /** Displays the form to edit the informations of a given submission. */
  def edit(id: IdType) = SlickAction(IsAuthorOf(id)) { implicit r =>
    val paper: Paper = Papers.withId(Id[Paper](id))
    val allTopics: List[Topic] = Topics.all
    val paperTopics: List[Topic] = Topics.of(paper)
    def incBind[T](form: Form[T], data: Map[String, String]) = form.bind(form.data ++ data)
    val existingSubmissionForm  = incBind(
      submissionForm.fill(SubmissionForm(paper, Authors.of(paper.id), List())),
      allTopics.zipWithIndex.filter(paperTopics contains _._1).map(ti =>
        (s"topics[${ti._2}]", ti._1.id.value.toString)).toMap
    )
    Ok(views.html.submissiontemplate("Editing Submission " + id.toString.take(4).toUpperCase, existingSubmissionForm, allTopics, routes.Submitting.doEdit(id), Navbar(r.user, Submitter))(Html("")))
  }
  
  /** Handles a new submission. Creates a database entry with the form data. */
  def doMake = SlickAction(IsSubmitter, parse.multipartFormData) { implicit r =>
    doSave(newId[Paper](), routes.Submitting.doMake)
  }
  
  /** Handles edit of a submission. Update the database entry with the form data. */
  def doEdit(id: IdType) = SlickAction(IsAuthorOf(id), parse.multipartFormData) { implicit r =>
    doSave(Id[Paper](id), routes.Submitting.doEdit(id))
  }
  
  private type Req = SlickRequest[MultipartFormData[play.api.libs.Files.TemporaryFile]]
  private def doSave(paperId: Id[Paper], errorEP: Call)(implicit r: Req) = {
    val now: DateTime = currentTime()
    // TODO: if the form is not js validated we might want to save the
    // uploaded file in case of errors. Otherwise the user will have to
    // select it again.
    submissionForm.bindFromRequest.fold(
      errors => Ok(views.html.submissiontemplate(
        "Submission: Errors found", errors, Topics.all, errorEP, Navbar(r.user, Submitter))(Html(""))),
      form => {
        val fileid: Option[Id[File]] = r.body.file("data").map{ file =>
          val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
          Files.ins(File((newId(), now, r.user.email), file.filename, blob.size, blob))
        }
        Papers.ins(form.paper.copy(metadata=(paperId, now, r.user.email), fileid=fileid))
        val personsId: List[Id[Person]] = Persons.insAll(
          form.authors.take(form.paper.nauthors).map(_.copy(metadata=(newId(), now, r.user.email))))
        Authors.insAll(personsId.zipWithIndex.map(pi =>
          Author((newId(), now, r.user.email), paperId, pi._1, pi._2)))
        PaperTopics.insAll(form.topics.map(i => PaperTopic((newId(), now, r.user.email), paperId, Id(i))))
        Redirect(routes.Submitting.info(paperId.value))
      }
    )
  }
}
