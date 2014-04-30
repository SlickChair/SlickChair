package controllers

import models.{ PersonRole, Persons, User }
import models.PersonRole._
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import play.api.db.slick.Config.driver.simple._
import Utils._
import models._

trait Authorization {
  def isAuthorized[A](implicit request: SlickRequest[A]): Boolean
}

object IsSubmitter extends Authorization {
  def isAuthorized[A](implicit request: SlickRequest[A]) = true
}

case class IsAuthorOf(id: models.IdType) extends Authorization {
  def isAuthorized[A](implicit request: SlickRequest[A]) =
    request.user.role == Chair || (Authors of Id[Paper](id) map (_.id) contains request.user.id)
}

object IsReviewer extends Authorization {
  def isAuthorized[A](implicit request: SlickRequest[A]) = request.user.role >= Reviewer
}

object IsChair extends Authorization {
  def isAuthorized[A](implicit request: SlickRequest[A]) = request.user.role >= Chair
}
