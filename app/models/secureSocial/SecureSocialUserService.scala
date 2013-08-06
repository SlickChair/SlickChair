package models.secureSocial

import play.api.Application
import securesocial.core.UserServicePlugin

/** Implements the UserServicePlugin required to use the SecureSocial plugin on
  * top of Slick. See http://securesocial.ws/guide/user-service.html for more
  * details.
  */
class SecureSocialUserService(application: Application) extends UserServicePlugin(application)
  with SecureSocialUsers.Queries
  with SecureSocialTokens.Queries