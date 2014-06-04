package controllers

import models.Role.{Chair, Role, PC_Member, Author}
import models.{Query, Configuration}
import play.api.templates.Html
import play.api.mvc.Call

object Navbar {
  def apply(currentRole: Role)(implicit r: SlickRequest[_]): Html = {
    case class M(call: Call, name: String, isEnabled: Configuration => Boolean)
    val userMenuMs: List[M] = currentRole match {
      case Chair =>
        List(
          M(routes.Chairing.submissions, "Submissions", _.alwaysEnabled),
          M(routes.Chairing.roles, "Roles", _.chairRoles),
          M(routes.Chairing.assignmentList, "Assignment", _.chairAssignment),
          M(routes.Chairing.decision, "Decision", _.chairDecision),
          M(routes.Sql.query, "SQL", _.chairSql))
      case PC_Member =>
        val papers = Query(r.db) assignedTo r.user.id map { p =>
          M(routes.Reviewing.review(p.id), "Submission " + Query(r.db).indexOf(p.id), _.alwaysEnabled)
        }
        M(routes.Reviewing.submissions, "Submissions", _.alwaysEnabled) ::
        M(routes.Reviewing.bid, "Bidding", _.pcmemberBid) ::
        papers
      case Author =>
        val papers = Query(r.db) papersOf r.user.id map { p =>
          M(routes.Submitting.info(p.id), "Submission " + Query(r.db).indexOf(p.id), _.alwaysEnabled)
        }
        M(routes.Submitting.submit, "New Submission", _.alwaysEnabled) ::
        papers
    }
    val conf = Query(r.db).configuration
    val userMenu = userMenuMs filter (_.isEnabled(conf)) map { m => (m.call, m.name) }
    val role = Query(r.db).roleOf(r.user.id)
    views.html.navbar(Some(r.user), Some(role), currentRole, userMenu, r.uri)
  }
  
  val empty = views.html.navbar(None, None, Author, Nil, "")
}
