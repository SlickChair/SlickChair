package controllers

import models.PersonRole._
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import Utils._
import models._
import models.BidValue._

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

case class IsAuthorOf(paperId: IdType) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    Query(r.db) authorsOf Id[Paper](paperId) map (_.id) contains r.user.id
  }
}

case class NonConflictingReviewer(paperId: IdType) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) =
    IsReviewer(r) && Query(r.db).bidsOf(r.user.id, Id[Paper](paperId)).filter(_.value == Conflict).isEmpty
}

case class AuthorOrNCReviewer(fileId: IdType) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    val paperid = Query(r.db).paperWithFile(Id[File](fileId)).id.value
    IsAuthorOf(paperid)(r) || NonConflictingReviewer(paperid)(r)
  }
}
