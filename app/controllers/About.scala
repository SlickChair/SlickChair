package controllers

import play.api.mvc.{Action, Controller}
import play.api.templates.Html
import scala.io.Source
import Utils._
import models.PersonRole.Submitter

object About extends Controller {
  def about = SlickAction(IsSubmitter) { implicit r =>
    Ok(views.html.main("About SlickChair", Navbar(Submitter))(Html(
      "<div class='row'><div class='col-md-10 col-sm-offset-1'>" +
      new eu.henkelmann.actuarius.ActuariusTransformer()(Source fromFile "README.md" mkString "") +
      "</div></div>"
    )))
  }
  def login = Action { Redirect(securesocial.controllers.routes.LoginPage.login) }
}
