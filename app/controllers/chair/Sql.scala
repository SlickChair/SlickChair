package controllers.chair

import anorm.SQL
import controllers.ChairOnly
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText, text, tuple }
import play.api.db.DB
import play.api.db.slick.{ DB => SlickDB }
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import controllers.Menu
import controllers.Utils.uEmail

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
  
  def form = SecuredAction { implicit request =>
    SlickDB withSession { implicit session =>
      Ok(views.html.chair.sql(None, queryForm, request.user.email.get, Menu(uEmail())))
    }
  }
  
  def runQuery = SecuredAction { implicit request =>
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
      SlickDB withSession { implicit s =>
        Ok(views.html.chair.sql(Some(result), filledForm, request.user.email.get, Menu(uEmail())))
      }
    }
  }
}
