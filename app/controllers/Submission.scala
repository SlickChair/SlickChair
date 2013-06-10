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

import models.entities._
// case class models.entities.Paper(
//   id: Int,
//   contactemail: String,
//   submissiondate: DateTime,
//   lastupdate: DateTime,
//   accepted: Option[Boolean],
//   title: String,
//   format: PaperFormat,
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
      "title" -> nonEmptyText,
      "format" -> nonEmptyText,
      "keywords" -> nonEmptyText,
      "abstrct" -> nonEmptyText
    )((title, format, keywords, abstrct) =>
        Paper(-1, "TOTO_FROM_SECURESOCIAL@gmail.com", DateTime.now, DateTime.now, None, title, PaperFormat.withName(format), keywords, abstrct, None))
     ((paper: Paper) =>
        Some((paper.title, paper.format.toString, paper.keywords, paper.abstrct)))
  )
  
  def form = Action(Ok(views.html.submit(paperForm)))

  def make = Action { implicit request =>
    paperForm.bindFromRequest.fold(
      // Form has errors, redisplay it
      errors => Ok(views.html.submit(errors)),
      
      paper => Ok(paper.toString)
    )
  }

  def info(id: Int) = TODO

  def edit(id: Int) = TODO
}