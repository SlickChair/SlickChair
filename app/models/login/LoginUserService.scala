package models.login

import play.api.Application
import securesocial.core.UserServicePlugin

/** Implements the UserServicePlugin required to use the SecureSocial plugin on
  * top of Slick. See http://securesocial.ws/guide/user-service.html for more
  * details.
  */
class LoginUserService(application: Application) extends UserServicePlugin(application)
  with LoginUsers.Queries
  with LoginTokens.Queries
