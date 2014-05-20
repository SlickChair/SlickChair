package controllers

import models._
import BidValue._
import PersonRole._

trait Authorization {
  def apply[A](implicit r: SlickRequest[A]): Boolean
}

object IsSubmitter extends Authorization {
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
    val paperid = Query(r.db).paperWithFile(fileId).id
    IsAuthorOf(paperid)(r) || NonConflictingReviewer(paperid)(r)
  }
}
