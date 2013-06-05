package service

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token

// needed?
import securesocial.core.UserId
import scala.Some
import org.mindrot.jbcrypt.BCrypt

class InMemoryUserService(application: Application) extends UserServicePlugin(application) {
  private var users = Map[String, Identity]()
  private var tokens = Map[String, Token]()

  /**
   * Finds a user that maches the specified id
   *
   * @param id the user id
   * @return an optional user
   */
  def find(id: UserId): Option[Identity] = {
    if (Logger.isDebugEnabled) Logger.debug("users = %s".format(users))
    users.get(id.id + id.providerId)
  }
  
  /**
   * Saves the user. This method gets called when a user logs in.
   * This is your chance to save the user information in your backing store.
   * @param user
   */
  def save(user: Identity): Identity = {
    users = users + (user.id.id + user.id.providerId -> user)
    // this sample returns the same user object, but you could return an instance of your own class
    // here as long as it implements the Identity trait. This will allow you to use your own class in the protected
    // actions and event callbacks. The same goes for the find(id: UserId) method.
    user
  }
  
  // UsernamePassword provider related methods
  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    if ( Logger.isDebugEnabled ) {
      Logger.debug("users = %s".format(users))
    }
    users.values.find( u => u.email.map( e => e == email && u.id.providerId == providerId).getOrElse(false))
  }
  
  def save(token: Token) {
    tokens += (token.uuid -> token)
  }

  def findToken(token: String): Option[Token] = {
    tokens.get(token)
  }

  def deleteToken(uuid: String) {
    tokens -= uuid
  }

  def deleteTokens() {
    tokens = Map()
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }
}