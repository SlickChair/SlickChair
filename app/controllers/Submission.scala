package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._
import securesocial.core.SecureSocial
import play.api.data.format.Formats._

import models.entities._
// case class Paper(
//   id: Int,
//   contactemail: String,
//   submissiondate: DateTime,
//   lastupdate: DateTime,
//   accepted: Option[Boolean],
//   title: String,
//   format: PaperFormat,
//   student: Boolean,
//   keywords: String,
//   abstrct: String,
//   data: Option[Array[Byte]]
// )

object Submission extends Controller with SecureSocial {
  // See playframework.com/documentation/2.1.1/ScalaForms (-> Constructing complex objects)
  // and github.com/playframework/Play20/blob/master/samples/scala/forms/app/controllers/SignUp.scala
  // to understand how From objects work in Play2.
  val paperForm = Form(
    mapping(
      "contactemail" -> nonEmptyText,
      "title" -> nonEmptyText,
      "format" -> nonEmptyText,
      "student" -> boolean,
      "keywords" -> nonEmptyText,
      "abstrct" -> nonEmptyText
    )((contactemail, title, format, student, keywords, abstrct) =>
        Paper(0, contactemail, null, null, None, title, PaperFormat.withName(format), student, keywords, abstrct, None))
     ((paper: Paper) =>
        Some((paper.contactemail, paper.title, paper.format.toString, paper.student, paper.keywords, paper.abstrct)))
  )
  
  def form = Action(Ok(views.html.submit(null)))

  def make = TODO

  def info(id: Int) = TODO

  def edit(id: Int) = TODO
  
  // val submitForm = Form(
  //   "title" -> String,
  //   "format" -> PaperFormat,
  //   "student" -> Boolean,
  //   "keywords" -> String,
  //   "abstrct" -> String,
  //   "data" -> Option[Array[Byte]]
  // )
}