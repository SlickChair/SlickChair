package controllers

import play.api.db.slick.Config.driver.simple._
import play.api.mvc.{ Call, Request }
import play.api.mvc.Request
import play.api.templates.Html
import models._
import models.PersonRole._
import securesocial.core.SecuredRequest
import Utils._

object Navbar {
  private val newSubmission = (routes.Submitting.make, "New Submission")  
  
  def apply(currentRole: PersonRole)(implicit r: SlickRequest[_]): Html = {
    val roleSpecificEntries = (currentRole match {
      case Chair =>
        List((routes.Sql.query, "SQL"))
      case Reviewer =>
        (routes.Reviewing.papers, "Submissions") :: 
        (routes.Reviewing.bid, "Bidding") :: Nil
      case Submitter =>
        val papers = Papers of r.user.email map (id =>
          (routes.Submitting.info(id.value), "Submission " + shorten(id.value)))
        newSubmission :: papers
    })
    views.html.navbar(Some(r.user), currentRole, roleSpecificEntries)
  }
  
  def notLoggedIn(implicit r: Request[Any]): Html =
    views.html.navbar(None, Submitter, List(newSubmission))
}
