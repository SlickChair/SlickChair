package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._

object Application extends Controller {
  
  def index = Action {
    Redirect(routes.Application.tasks)
    // Ok("supson") 
  }
  
  def login = TODO
  
    
  def tasks = Action {
    Ok(views.html.index(Tasks.all, taskForm))
  }
  
  def newTask = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(null, errors)),
      label => {
        Tasks.insert(label)
        Redirect(routes.Application.tasks)
      }
    )
  }
  
  def deleteTask(id: Long) = Action {
    Tasks.delete(id)
    Redirect(routes.Application.tasks)
  }
  
  val taskForm = Form(
    "label" -> nonEmptyText
  )
}