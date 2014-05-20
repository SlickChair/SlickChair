package controllers

import play.api.mvc.{ Call, Request }
import play.api.mvc.Request
import play.api.templates.Html
import models._
import models.PersonRole._
import securesocial.core.SecuredRequest
import models.Mappers._

object Navbar {
  private val newSubmission = (routes.Submitting.submit, "New Submission")  
  
  def apply(currentRole: PersonRole)(implicit r: SlickRequest[_]): Html = {
    val roleSpecificEntries = (currentRole match {
      case Chair =>
        List((routes.Sql.query, "SQL"))
      case Reviewer =>
        // (routes.Reviewing.papers, "Submissions") :: 
        (routes.Reviewing.bid, "Bidding") :: Nil
      case Submitter =>
        val papers = Query(r.db) papersOf r.user.id map { p =>
          (routes.Submitting.info(p.id), "Submission " + Query(r.db).indexOf(p.id))
        }
        newSubmission :: papers
    })
    views.html.navbar(Some(r.user), Some(Query(r.db).roleOf(r.user.id)), currentRole, roleSpecificEntries)
  }
  
  val empty = views.html.navbar(None, None, models.PersonRole.Submitter, Nil)(null)
}
