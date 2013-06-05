package models

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import securesocial.core._
import securesocial.core.providers.Token
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime

// case class securesocial.core.providers.Token(
//   uuid: String,
//   email: String,
//   creationTime: org.joda.time.DateTime,
//   expirationTime: org.joda.time.DateTime,
//   isSignUp: Boolean
// )

object Tokens extends Table[Token]("TOKENS") {
  def uuid = column[String]("UUID", O.PrimaryKey)
  def email = column[String]("EMAIL")
  def creationTime = column[DateTime]("CREATION_TIME")
  def expirationTime = column[DateTime]("EXPIRATION_TIME")
  def isSignUp = column[Boolean]("IS_SIGN_UP")
  def * = uuid ~ email ~ creationTime ~ expirationTime ~ isSignUp <> (Token.apply _, Token.unapply _)

  // Operations
  def save(token: Token): Token = DB.withTransaction { implicit session =>
    findToken(token.uuid) match {
      case None =>
        this.insert(token)
      case Some(t) =>
        Tokens.where(_.uuid is t.uuid).update(token)
    }
    token
  }

  def deleteToken(uuid: String) = DB.withTransaction { implicit session =>
    this.where(_.uuid is uuid).mutate(_.delete)
  }

  def deleteTokens() = DB.withTransaction { implicit session =>
    Query(Tokens).mutate(_.delete)
  }

  def deleteExpiredTokens() = DB.withTransaction { implicit session =>
    Tokens.filter(_.expirationTime <= DateTime.now).mutate(_.delete)
  }

  def findToken(uuid: String): Option[Token] = DB.withSession { implicit session =>
    createFinderBy(_.uuid).apply(uuid).firstOption
  }
}