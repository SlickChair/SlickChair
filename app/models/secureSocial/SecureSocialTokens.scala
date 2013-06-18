package models.secureSocial

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import java.sql.Date
import org.joda.time.DateTime
import securesocial.core._
import securesocial.core.providers.Token

// case class securesocial.core.providers.Token(
//   uuid: String,
//   email: String,
//   creationTime: org.joda.time.DateTime,
//   expirationTime: org.joda.time.DateTime,
//   isSignUp: Boolean
// )

object SecureSocialTokens extends Table[Token]("SECURE_SOCIAL_TOKENS") {
  def uuid = column[String]("UUID", O.DBType("TEXT"), O.PrimaryKey)
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def creationTime = column[DateTime]("CREATIONTIME")
  def expirationTime = column[DateTime]("EXPIRATIONTIME")
  def isSignUp = column[Boolean]("ISSIGNUP")
  def * = uuid ~ email ~ creationTime ~ expirationTime ~ isSignUp <> (Token, Token.unapply _)

  trait Queries {
    def save(token: Token): Unit = DB.withTransaction { implicit session =>
      findToken(token.uuid) match {
        case None => SecureSocialTokens.insert(token)
        case Some(t) => SecureSocialTokens.filter(_.uuid is t.uuid).update(token)
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