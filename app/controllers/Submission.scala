package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._
import securesocial.core.SecureSocial
import play.api.data.format.Formats._
import org.joda.time.DateTime
import java.io._

import models.entities._
import models.entities.PaperFormat._

case class SubmissionForm(
  paper: Paper,
  authors: List[Author],
  topics: List[Int]
)

object Submission extends Controller with SecureSocial {
  // See playframework.com/documentation/2.1.1/ScalaForms (-> Constructing complex objects)
  // and github.com/playframework/Play20/blob/master/samples/scala/forms/app/controllers/SignUp.scala
  // to understand how Froms work in Play2.
  val paperFormatMapping: Mapping[PaperFormat] =
    mapping("value" -> nonEmptyText)(PaperFormat.withName(_))(Some(_).map(_.toString))
  
  val paperMapping: Mapping[Paper] = mapping(
    "id" -> ignored(-1),
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
      "authors" -> list(authorMapping),
      "topics" -> list(number).verifying("Please select at least one topic.", !_.isEmpty)
    )((paper, nauthors, authors, topics) =>
        SubmissionForm(paper, authors.take(nauthors).zipWithIndex.map{case (a, i) => a.copy(position = i)}, topics))
     (submissionForm =>
        Some(submissionForm.paper, submissionForm.authors.size, submissionForm.authors, submissionForm.topics))
  )
  
  def form = Action(Ok(views.html.submit(submissionForm)))

  def make = Action(parse.multipartFormData) { implicit request =>
    val tmpFile = request.body.file("data").map(_.ref.file)
    val blob = tmpFile.map(scalax.io.Resource.fromFile(_).byteArray)
    
    submissionForm.bindFromRequest.fold(
      errors => Ok(views.html.submit(errors)),
      filled => filled match {
        case SubmissionForm(paper, authors, topics) => 
          Ok(filled.toString)
      }
    )
  }

  def info(id: Int) = TODO

  def edit(id: Int) = TODO
}