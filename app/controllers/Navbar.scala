package controllers

import play.api.db.slick.Config.driver.simple._
import play.api.mvc.{ Call, Request }
import play.api.mvc.Request
import play.api.templates.Html
import models._
import models.PersonRole._
import securesocial.core.SecuredRequest

object Navbar {
  private val newSubmission = (routes.Submitting.make, "New Submission")  
  private val home = (chair.routes.Doc.umentation, "SlickChair Demo")
  
  def apply(user: Person, currentRole: PersonRole)(implicit s: Session, r: Request[Any]): Html = {
    val roleSpecificEntries = (currentRole match {
      case Chair =>
       List((chair.routes.Sql.form, "SQL"))
      case Reviewer =>
        Nil
      case Submitter =>
        val papers = Papers of user.email map (id =>
          (routes.Submitting.info(id.value), "Submission " + id.value.toString.take(4).toUpperCase))
        newSubmission :: papers
    })
    views.html.navbar(Some(user), currentRole, home :: roleSpecificEntries)
  }
  
  def notLoggedIn(implicit r: Request[Any]): Html =
    views.html.navbar(None, Submitter, List(home, newSubmission))
}
