package models.secureSocial

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import java.sql.Date
import org.joda.time.DateTime
import securesocial.core._
import securesocial.core.providers.Token

case class MyToken(
  uuid: String,
  email: String,
  creationTime: org.joda.time.DateTime,
  expirationTime: org.joda.time.DateTime,
  isSignUp: Boolean,
  isInvitation: Boolean
) {
  def toT = Token(uuid, email, creationTime, expirationTime, isSignUp)
}
object MyToken {
  def fromT(t: Token) = MyToken(t.uuid, t.email, t.creationTime, t.expirationTime, t.isSignUp, false)
}

object SecureSocialTokens extends Table[MyToken]("SECURE_SOCIAL_TOKENS") {
  def uuid = column[String]("UUID", O.DBType("TEXT"), O.PrimaryKey)
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def creationTime = column[DateTime]("CREATIONTIME")
  def expirationTime = column[DateTime]("EXPIRATIONTIME")
  def isSignUp = column[Boolean]("ISSIGNUP")
  def isInvitation = column[Boolean]("ISINVITATION")
  def * = uuid ~ email ~ creationTime ~ expirationTime ~ isSignUp ~ isInvitation <> (MyToken.apply _, MyToken.unapply _)
  
  def allInvitations = DB.withSession { implicit session =>
    Query(SecureSocialTokens).filter(_.isInvitation).list }
  
  def ins(myToken: MyToken) = DB.withSession { implicit session =>
    SecureSocialTokens.insert(myToken) }
  
  def del(uuid: String) = DB.withTransaction { implicit session =>
    SecureSocialTokens.filter(_.uuid is uuid).delete }
    
  trait Queries {
    def save(token: Token): Unit = DB.withTransaction { implicit session =>
      findToken(token.uuid) match {
        case None => SecureSocialTokens.insert(MyToken.fromT(token))
        case Some(t) => SecureSocialTokens.filter(_.uuid is t.uuid).update(MyToken.fromT(token))
      }
    }

    def deleteToken(uuid: String): Unit = DB.withTransaction { implicit session =>
      SecureSocialTokens.filter(_.uuid is uuid).delete }

    def deleteExpiredTokens: Unit = DB.withTransaction { implicit session =>
      SecureSocialTokens.filter(_.expirationTime <= DateTime.now).delete }

    def findToken(uuid: String): Option[Token] = DB.withSession { implicit session =>
      createFinderBy(_.uuid).apply(uuid).firstOption.map(_.toT) }
  }
}