package models.secureSocial

import play.api._
import securesocial.core._

class SecureSocialUserService(application: Application) extends UserServicePlugin(application)
  with SecureSocialUsers.Queries
  with SecureSocialTokens.Queries