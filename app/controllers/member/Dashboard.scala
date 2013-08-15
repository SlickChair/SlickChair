package controllers.member

import org.joda.time.DateTime
import controllers.{Anyone, MemberOrChair}
import models.entities.{Authors, MemberRole, Members, NewMember, Papers}
import models.securesocial.{SecureSocialTokens, User}
import play.api.mvc.Controller
import securesocial.core.SecureSocial

object Dashboard extends Controller with SecureSocial {
  def papers = SecuredAction(MemberOrChair) { implicit request =>
    Ok(views.html.member.paperList(
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
