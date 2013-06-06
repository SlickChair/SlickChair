package service

import play.api._
import securesocial.core._
import models._

class SecureSocialUserService(application: Application) extends UserServicePlugin(application)
  with SecureSocialUsers.Queries
  with SecureSocialTokens.Queries