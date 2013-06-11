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



object Chair extends Controller with SecureSocial {
  def sql = Action(Ok(views.html.sql(None, queryForm)))
  def runQuery = Action { implicit request =>
    val filledForm = queryForm.bindFromRequest
    DB.withConnection { implicit session =>
      //       val result = SQL(filledForm.get).executeInsert().toString
      val result = SQL(filledForm.get).apply().map(_.asList).toList.mkString("\n")
      Ok(views.html.sql(Some(result), filledForm))
    }
  }
  
  val queryForm = Form(
    "query" -> text
  )
}