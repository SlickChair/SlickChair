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

object IsReviewer extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    val role = Query(r.db) roleOf r.user.id
    role == Reviewer || role == Chair
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

case class NonConflictingReviewer(paperId: Id[Paper]) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) =
    IsReviewer(r) && Query(r.db).bidsOf(r.user.id, paperId).filter(_.value == Conflict).isEmpty
}

case class AuthorOrNCReviewer(fileId: Id[File]) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    val paperId = Query(r.db).paperWithFile(fileId).id
    IsAuthorOf(paperId)(r) || NonConflictingReviewer(paperId)(r)
  }
}
