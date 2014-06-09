package controllers

import eu.henkelmann.actuarius.ActuariusTransformer
import models.Role.Author
import play.api.mvc.{Action, Controller}
import play.api.templates.Html
import models._
import Role._

object About extends Controller {
  def about = SlickAction(IsAuthor, _ => true) { implicit r =>
    Ok(views.html.main("About SlickChair", Navbar(Author))(Html(
      "<div class='row'><div class='col-md-10 col-sm-offset-1'>" +
      new ActuariusTransformer()(io.Source fromFile "README.md" mkString "") +
      "</div></div>"
    )))
  }
  def login = Action { Redirect(securesocial.controllers.routes.LoginPage.login) }
  def loginDispatch = SlickAction(IsAuthor, _ => true) { implicit r =>
    Query(r.db) roleOf r.user.id match {
      case Author => Redirect(routes.Submitting.submit)
      case PC_Member => Redirect(routes.Reviewing.submissions)
      case Chair => Redirect(routes.Chairing.submissions)
    }
  }
}
