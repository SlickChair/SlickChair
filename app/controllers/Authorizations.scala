package controllers

import models.entities.{MemberRole, Members}
import securesocial.core.{Authorization, Identity}
import play.api.mvc._
import securesocial.core._
import models.entities._
import models.securesocial._

object ChairOnly extends Authorization {
  def isAuthorized(user: Identity) =
    Members.withEmail(user.email.get).filter(_.role == MemberRole.Chair).nonEmpty
}

object MemberOrChair extends Authorization {
  def isAuthorized(user: Identity) =
    Members.withEmail(user.email.get).filter(_.role != MemberRole.Disabled).nonEmpty
}

object Anyone extends Authorization {
  def isAuthorized(user: Identity) = true
}

object FakeAuth extends Controller {
  def FakeAction(a: Authorization)(f: SecuredRequest[AnyContent] => Result): Action[AnyContent] = Action(parse.anyContent) {
    implicit request => {
      val id = User("4@4", "userpass", "4@4", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None).toIdentity
      f(SecuredRequest(id, request))
    }
  }
  
  def FakeAwareAction(f: RequestWithUser[AnyContent] => Result): Action[AnyContent] = Action(parse.anyContent) {
    implicit request => {
      val id = User("4@4", "userpass", "4@4", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None).toIdentity
      f(RequestWithUser(Some(id), request))
    }
  }
}
