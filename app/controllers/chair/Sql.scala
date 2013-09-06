package controllers.chair

import anorm.SQL
import controllers.ChairOnly
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, text, tuple}
import play.api.db.DB
import play.api.mvc.Controller
import securesocial.core.SecureSocial

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
  
  def form = controllers.FakeAuth.FakeAction(ChairOnly) { implicit request =>
    Ok(views.html.chair.sql(None, queryForm, request.user.email.get))
  }
  
  def runQuery = controllers.FakeAuth.FakeAction(ChairOnly) { implicit request =>
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
      Ok(views.html.chair.sql(Some(result), filledForm, request.user.email.get))
    }
  }
}
