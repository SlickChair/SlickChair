package controllers

import eu.henkelmann.actuarius.ActuariusTransformer
import models.PersonRole.Submitter
import play.api.mvc.{Action, Controller}
import play.api.templates.Html

object About extends Controller {
  def about = SlickAction(IsSubmitter) { implicit r =>
    Ok(views.html.main("About SlickChair", Navbar(Submitter))(Html(
      "<div class='row'><div class='col-md-10 col-sm-offset-1'>" +
      new ActuariusTransformer()(io.Source fromFile "README.md" mkString "") +
      "</div></div>"
    )))
  }
  def login = Action { Redirect(securesocial.controllers.routes.LoginPage.login) }
}
