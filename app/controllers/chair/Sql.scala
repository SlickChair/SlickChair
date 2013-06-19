package controllers.chair

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
import controllers._

object SqlMethod extends Enumeration {
  type SqlMethod = Value
  val Execute, Update, Insert = Value
}
import SqlMethod._

object Sql extends Controller with SecureSocial {
  val queryForm = Form(
    tuple(
      "query" -> text,
      "method" -> mapping(
          "m" -> nonEmptyText
        )(SqlMethod.withName(_))
         (Some(_).map(_.toString))
    )
  ).fill(("", Execute))
  
  def form = SecuredAction(ChairOnly) { implicit request =>
    Ok(views.html.sql(None, queryForm))
  }
  
  def runQuery = SecuredAction(ChairOnly) { implicit request =>
    val filledForm = queryForm.bindFromRequest
    DB.withConnection { implicit session =>
      val (query, method) = filledForm.get
      val result: String = try {
        method match {
          // Fun fact: removing the .toStrings will crash the Scala compiler
          // with a java.lang.NullPointerException:
          // scala.tools.nsc.typechecker.Typers$Typer.adapt(Typers.scala:1131)
          case Execute => SQL(query.toUpperCase).apply().map(_.asList).toList.mkString("\n")
          case Update => SQL(query.toUpperCase).executeUpdate().toString
          case Insert => SQL(query.toUpperCase).executeInsert().toString
        }
      } catch {
        case e: Exception => e.toString.replaceFirst(": ", ":\n")
      }
      Ok(views.html.sql(Some(result), filledForm))
    }
  }
  
  def email = TODO
  def config = TODO
  def topics = TODO
  def members = TODO
  def auctions = TODO
  def decisions = TODO
  def dashboard = TODO
}