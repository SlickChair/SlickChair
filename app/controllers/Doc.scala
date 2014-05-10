package controllers

import play.api.mvc.{Action, Controller}

object Doc extends Controller {
  def umentation = Action {
    Ok(views.html.doc())
  }
}
