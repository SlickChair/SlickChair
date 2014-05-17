package controllers

import anorm.SQL
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText, text, tuple }
import play.api.db.DB
import play.api.db.slick.{ DB => SlickDB }
import play.api.mvc.Controller
import models.PersonRole._
import Utils._

object SqlMethod extends Enumeration {
  type SqlMethod = Value
  val Execute, Update, Insert = Value
}
import SqlMethod._

object Sql extends Controller {
  def queryForm = Form(
    tuple(
      "query" -> text,
      "method" -> mapping(
          "m" -> nonEmptyText
        )(SqlMethod.withName(_))
         (Some(_).map(_.toString))
    )
  ).fill(("", Execute))
  
  def query = SlickAction(IsChair) { implicit r =>
    Ok(views.html.sql(None, queryForm, Navbar(Chair)))
  }
  
  def doQuery = SlickAction(IsChair) { implicit r =>
    val filledForm = queryForm.bindFromRequest
    DB withConnection { implicit session =>
      val (query, method) = filledForm.get
      val result: String = try {
        method match {
          case Execute => SQL(query.toUpperCase).apply().map(_.asList).toList.mkString("\n")
          case Update => SQL(query.toUpperCase).executeUpdate().toString
          case Insert => SQL(query.toUpperCase).executeInsert().toString
        }
      } catch {
        case e: Exception => e.toString.replaceFirst(": ", ":\n")
      }
      Ok(views.html.sql(Some(result), filledForm, Navbar(Chair)))
    }
  }
}
