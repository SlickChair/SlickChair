package controllers.chair

import play.api._
import play.api.mvc._

object Doc extends Controller {
  def umentation = Action {
    Ok(views.html.doc())
  }
}
