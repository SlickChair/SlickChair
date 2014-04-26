package controllers

import play.api.db.slick.Config.driver.simple._
import play.api.mvc.{ Call, Request }
import play.api.mvc.Request
import play.api.templates.Html
import models._
import models.PersonRole._

object Navbar {
  private val newSubmission = (routes.Submitting.make, "New Submission")  
  private val home = (chair.routes.Doc.umentation, "SlickChair Demo")
  
  // List[(Call, String)]
  def apply(user: models.Person)(implicit session: Session, request: Request[Any]): Html = {
    val roleSpecificEntries = (user.role match {
      case Submitter =>
        val papers = Papers of user.email map (id =>
          (routes.Submitting.info(id.value), "Submission " + id.value.toString.take(4).toUpperCase))
        newSubmission :: papers
      case Reviewer =>
        Nil
      case Chair =>
       List((chair.routes.Sql.form, "SQL"))
    })
    views.html.navbar(Some(user), home :: roleSpecificEntries)
  }
  
  def notLoggedIn(implicit request: Request[Any]): Html = views.html.navbar(None, List(home, newSubmission))
}
