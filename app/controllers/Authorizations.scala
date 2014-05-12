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
  def apply[A](implicit r: SlickRequest[A]) = r.user.role == Reviewer || r.user.role == Chair
}

object IsChair extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = r.user.role == Chair
}

case class IsAuthorOf(paperId: IdType) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    play.api.Logger.error((Authors of Id[Paper](paperId)).toString)
    Authors of Id[Paper](paperId) map (_.id) contains r.user.id
    true
  }
}

case class NonConflictingReviewer(paperId: IdType) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) =
    IsReviewer(r) && Bids.of(r.user.id, Id[Paper](paperId)).filter(_.value == Conflict).isEmpty
}

case class AuthorOrNCReviewer(fileId: IdType) extends Authorization {
  def apply[A](implicit r: SlickRequest[A]) = {
    val paperid = Papers.withFile(Id[File](fileId)).value
    IsAuthorOf(paperid)(r) || NonConflictingReviewer(paperid)(r)
  }
}
