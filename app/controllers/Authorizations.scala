package controllers

import models.entities.{MemberRole, Members}
import securesocial.core.{Authorization, Identity}

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