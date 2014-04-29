package controllers

import models.{ PersonRole, Persons, User }
import models.PersonRole._
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import play.api.db.slick.Config.driver.simple._
import Utils._

trait Authorization {
  def isAuthorized[A](request: SlickRequest[A]): Boolean
}

object IsSubmitter extends Authorization {
  def isAuthorized[A](request: SlickRequest[A]) = { request.user.role >= Submitter; true }
}

object IsReviewer extends Authorization {
  def isAuthorized[A](request: SlickRequest[A]) = request.user.role >= Reviewer
}

object IsChair extends Authorization {
  def isAuthorized[A](request: SlickRequest[A]) = request.user.role >= Chair
}
