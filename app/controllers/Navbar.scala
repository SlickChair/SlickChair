package controllers

import models.PersonRole.{Chair, PersonRole, Reviewer, Submitter}
import models.Query
import play.api.templates.Html

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
