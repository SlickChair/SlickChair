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

object SecureSocialTokens extends Table[Token]("SECURE_SOCIAL_TOKENS") {
  def uuid = column[String]("UUID", O.PrimaryKey)
  def email = column[String]("EMAIL")
  def creationTime = column[DateTime]("CREATION_TIME")
  def expirationTime = column[DateTime]("EXPIRATION_TIME")
  def isSignUp = column[Boolean]("IS_SIGN_UP")
  def * = uuid ~ email ~ creationTime ~ expirationTime ~ isSignUp <> (Token.apply _, Token.unapply _)

  trait Queries {
    def save(token: Token): Unit = DB.withTransaction { implicit session =>
      findToken(token.uuid) match {
        case None =>
          SecureSocialTokens.insert(token)
        case Some(t) =>
          SecureSocialTokens.filter(_.uuid is t.uuid).update(token)
      }
    }

    def deleteToken(uuid: String): Unit = DB.withTransaction { implicit session =>
      SecureSocialTokens.filter(_.uuid is uuid).delete
    }

    def deleteTokens(): Unit = DB.withTransaction { implicit session =>
      Query(SecureSocialTokens).delete
    }

    def deleteExpiredTokens():Unit = DB.withTransaction { implicit session =>
      SecureSocialTokens.filter(_.expirationTime <= DateTime.now).delete
    }

    def findToken(uuid: String): Option[Token] = DB.withSession { implicit session =>
      createFinderBy(_.uuid).apply(uuid).firstOption
    }
  }
}