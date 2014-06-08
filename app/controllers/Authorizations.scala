package controllers

import models._
import BidValue._
import Role._

trait Authorization {
  def apply[A](implicit r: SlickRequest[A]): Boolean
}

object IsAuthor extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = true
}

object IsPCMember extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    val role = Query(r.db) roleOf r.user.id
    role == PC_Member || role == Chair
  }
}

object IsChair extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = Query(r.db).roleOf(r.user.id) == Chair
}

case class IsAuthorOf(paperId: Id[Paper]) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    Query(r.db) authorsOf paperId map (_.id) contains r.user.id
  }
}

case class IsAuthorOfNotWithdrawn(paperId: Id[Paper]) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    IsAuthorOf(paperId).apply && !Query(r.db).paperWithId(paperId).withdrawn
  }
}

case class NonConflictingPCMember(paperId: Id[Paper]) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) =
    IsPCMember(r) && Query(r.db).bidsOf(r.user.id, paperId).filter(_.value == Conflict).isEmpty
}

case class AuthorOrNCPCMember(fileId: Id[File]) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    val paperId = Query(r.db).paperWithFile(fileId).id
    IsAuthorOf(paperId)(r) || NonConflictingPCMember(paperId)(r)
  }
}
