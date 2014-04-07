package controllers

import models.{MemberRole, Persons}
import models.User
import play.api.mvc.{Action, AnyContent, Controller, Result}
import securesocial.core.{Authorization, Identity, RequestWithUser, SecuredRequest}
import play.api.db.slick.Config.driver.simple._

/** Authorization checking that the user is chair. */
object ChairOnly extends Authorization {
  def isAuthorized(user: Identity): Boolean =
    false
    // Persons.withEmail(user.email.get).filter(_.role == MemberRole.Chair).nonEmpty TODO
}

/** Authorization checking that the user is member or chair. */
object MemberOrChair extends Authorization {
  def isAuthorized(user: Identity): Boolean =
    false // Persons.withEmail(user.email.get).filter(_.role != MemberRole.Disabled).nonEmpty TODO
}

/** Authorization for anyone. */
object Anyone extends Authorization {
  def isAuthorized(user: Identity) = true
}

/** Fake authorization for off-line development and testing. */
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
