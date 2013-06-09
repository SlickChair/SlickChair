package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._
import securesocial.core.SecureSocial

object Application extends Controller with SecureSocial {
  def index = Action {
    Redirect(routes.Application.tasks)
  }
  
  def tasks = Action {
    Ok(views.html.index(Tasks.all, taskForm))
  }
  
  // TODO: add authorization http://securesocial.ws/guide/authorization.html
  def newTask = SecuredAction { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(Tasks.all, errors)),
      label => {
        Tasks.insert(label + "@" + request.user)
        Redirect(routes.Application.tasks)
      }
    )
  }
  
  def deleteTask(id: Long) = SecuredAction { implicit request =>
    // request.user
    Tasks.delete(id)
    Redirect(routes.Application.tasks)
  }
  
  val taskForm = Form(
    "label" -> nonEmptyText
  )
}