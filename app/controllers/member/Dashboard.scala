package controllers.member

import org.joda.time.DateTime
import models.secureSocial._
import models.entities._
import models.relations.{Comment, Comments, NewComment, Review, ReviewConfidence}
import models.relations.ReviewConfidence.ReviewConfidence
import models.relations.ReviewEvaluation
import models.relations.ReviewEvaluation.ReviewEvaluation
import models.relations.Reviews
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, nonEmptyText}
import play.api.data.Mapping
import play.api.mvc.{Controller, Result}
import securesocial.core.{SecureSocial, SecuredRequest}
import controllers._

object Dashboard extends Controller with SecureSocial {
  def papers = SecuredAction(MemberOrChair) { implicit request =>
    Ok(views.html.paperList(
      Papers.all.map(p => (p, Authors.of(p)))
    ))
  }
  
  def invite(uuid: String) = SecuredAction(Anyone) { implicit request =>
    SecureSocialTokens.withUUID(uuid) match {
      case None => BadRequest("Token expired.")
      case Some(token) =>
        val user = User.fromIdentity(request.user)
        Members.withEmail(user.email) match {
          case Some(_) => BadRequest("Already a member.")
          case None => 
            SecureSocialTokens.del(uuid)
            val now = DateTime.now
            Members.ins(NewMember(
              user.email,
              token.email,
              now,
              now,
              MemberRole.Member,
              user.firstname,
              user.lastname
            ))
            Redirect(controllers.member.routes.Dashboard.papers)
        }
    }
  }
}