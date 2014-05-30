package controllers

import models.Role.{Chair, Role, Reviewer, Author}
import models.Query
import play.api.templates.Html

object Navbar {
  private val newSubmission = (routes.Submitting.submit, "New Submission")  
  
  def apply(currentRole: Role)(implicit r: SlickRequest[_]): Html = {
    val roleSpecificEntries = (currentRole match {
      case Chair =>
        List(
          (routes.Chairing.assignmentList, "Assignment"),
          (routes.Chairing.decision, "Decision"),
          (routes.Sql.query, "SQL"))
      case Reviewer =>
        val papers = Query(r.db) assignedTo r.user.id map { p =>
          (routes.Reviewing.review(p.id), "Submission " + Query(r.db).indexOf(p.id))
        }
        (routes.Reviewing.bid, "Bidding") :: papers
      case Author =>
        val papers = Query(r.db) papersOf r.user.id map { p =>
          (routes.Submitting.info(p.id), "Submission " + Query(r.db).indexOf(p.id))
        }
        newSubmission :: papers
    })
    views.html.navbar(Some(r.user), Some(Query(r.db).roleOf(r.user.id)), currentRole, roleSpecificEntries)
  }
  
  val empty = views.html.navbar(None, None, Author, Nil)(null)
}
