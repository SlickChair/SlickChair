package controllers

import securesocial.core.{Identity, Authorization}
import models.entities._
import play.api.Logger

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