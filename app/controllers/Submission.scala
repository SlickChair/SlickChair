package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import securesocial.core.SecureSocial
import org.joda.time.DateTime

import models._
import models.entities._
import models.relations._
import models.entities.PaperFormat._

case class SubmissionForm(
  paper: Paper,
  authors: Seq[Author],
  topics: Seq[Int]
)

object Submission extends Controller with SecureSocial {
  // See playframework.com/documentation/2.1.1/ScalaForms (-> Constructing complex objects)
  // and github.com/playframework/Play20/blob/master/samples/scala/forms/app/controllers/SignUp.scala
  // to understand how Froms work in Play2.
  val paperFormatMapping: Mapping[PaperFormat] = mapping(
    "value" -> nonEmptyText)(PaperFormat.withName(_))(Some(_).map(_.toString))
  
  val paperMapping: Mapping[Paper] = mapping(
    "id" -> ignored(Option.empty[Int]),
    "contactemail" -> ignored("TODO_FROM_SECURESOCIAL@gmail.com"),
    "submissiondate" -> ignored(DateTime.now),
    "lastupdate" -> ignored(DateTime.now),
    "accepted" -> ignored(Option.empty[Boolean]),
    "title" -> nonEmptyText,
    "format" -> paperFormatMapping,
    "keywords" -> nonEmptyText,
    "abstrct" -> nonEmptyText,
    "paperid" -> ignored(Option.empty[Int])
  )(Paper.apply _)(Paper.unapply _)
  
  val authorMapping: Mapping[Author] = mapping(
    "paperid" -> ignored(-1),
    "position" -> ignored(-1),
    "firstname" -> text,
    "lastname" -> text,
    "organization" -> text,
    "email" -> text
  )(Author.apply _)(Author.unapply _)
    
  val submissionForm: Form[SubmissionForm] = Form(
    mapping(
      "paper" -> paperMapping,
      "nauthors" -> number,
      "authors" -> seq(authorMapping),
      "topics" -> seq(number).verifying("Please select at least one topic.", !_.isEmpty)
    )((paper, nauthors, authors, topics) =>
        SubmissionForm(paper, authors.take(nauthors).zipWithIndex.map{case (a, i) => a.copy(position = i)}, topics))
     (submissionForm =>
        Some(submissionForm.paper, submissionForm.authors.size, submissionForm.authors, submissionForm.topics))
  )
  
  def form = Action(Ok(views.html.submission("New Submission", routes.Submission.make, submissionForm)))

  def make = Action(parse.multipartFormData) { implicit request =>
    submissionForm.bindFromRequest.fold(
      errors => Ok(views.html.submission(request.body.file("data").get.filename  + "New Submission: Errors found", routes.Submission.make, errors)),
      filled => filled match {
        case SubmissionForm(paper, authors, topics) => 
          val fileId = request.body.file("data").map{ file =>
            val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
            Files.ins(File(None, file.filename, blob.size, DateTime.now, blob))
          }
          val paperId: Int = Papers.ins(paper.copy(fileId))
          authors.foreach(author => Authors.ins(author.copy(paperId)))
          topics.foreach(topic => PaperTopics.ins(PaperTopic(paperId, topic)))
          Ok(filled.toString)
      }
    )
  }
  
  def edit(id: Int) = Action(
    Papers.withId(id) match {
      case None =>
        NotFound
      case Some(paper) =>
        val s = submissionForm.fill(SubmissionForm(paper, Authors.of(id), Seq()))
        val existingSubmission = s.bind(s.data ++ PaperTopics.of(id).map(i => (s"topics[$i]", i.toString)))
        Ok(views.html.submission("Edit Submission", routes.Submission.makeEdit(id), existingSubmission))
    }
  )
  
  def makeEdit(id: Int) = TODO
  
  def info(id: Int) = TODO
}