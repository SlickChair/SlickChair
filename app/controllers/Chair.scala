package controllers

import play.api.db._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._
import securesocial.core.SecureSocial
import anorm._
import anorm.SqlParser._

object SqlMethod extends Enumeration {
  type SqlMethod = Value
  val Execute, Update, Insert = Value
}
import SqlMethod._

object Chair extends Controller with SecureSocial {
  val queryForm = Form(
    tuple(
      "query" -> text,
      "method" -> mapping(
          "m" -> nonEmptyText
        )(SqlMethod.withName(_))
         (Some(_).map(_.toString))
    )
  ).fill(("", Execute))
  
  def sql = Action(Ok(views.html.sql(None, queryForm)))
  def runQuery = Action { implicit request =>
    val filledForm = queryForm.bindFromRequest
    DB.withConnection { implicit session =>
      val (query, method) = filledForm.get
      val result: String = try {
        method match {
          case Execute => SQL(query).apply().map(_.asList).toList.mkString("\n")
          case Update => SQL(query).executeUpdate().toString
          case Insert => SQL(query).executeInsert().toString
        }
      } catch {
        case e: Exception => e.toString.replaceFirst(": ", ":\n")
      }
      Ok(views.html.sql(Some(result), filledForm))
    }
  }
}