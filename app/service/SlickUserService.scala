package service

import _root_.models._
import play.api._
import securesocial.core._
import securesocial.core.providers.Token
import models._

class SlickUserService(application: Application) extends UserServicePlugin(application) {
  def find(id: UserId): Option[Identity] =
    SocialUsers.find(id)

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] =
    SocialUsers.findByEmailAndProvider(email, providerId)

  def save(identity: Identity): Identity =
    SocialUsers.save(identity)

  def save(token: Token) =
    Tokens.save(token)
 
  def findToken(token: String): Option[Token] =
    Tokens.findToken(token)
 
  def deleteToken(uuid: String) =
    Tokens.deleteToken(uuid)
 
  def deleteTokens() = 
    Tokens.deleteTokens()
 
  def deleteExpiredTokens() =
    Tokens.deleteExpiredTokens()
}