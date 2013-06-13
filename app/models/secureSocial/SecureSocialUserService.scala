package models.secureSocial

import play.api._
import securesocial.core._

/**
 * Implements the UserServicePlugin required to use the SecureSocial plugin on
 * top of Slick. See http://securesocial.ws/guide/user-service.html for more
 * details.
 */
class SecureSocialUserService(application: Application) extends UserServicePlugin(application)
  with SecureSocialUsers.Queries
  with SecureSocialTokens.Queries
