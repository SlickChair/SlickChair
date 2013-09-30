package models.login

import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport.dateTimeTypeMapper
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import securesocial.core.providers.Token

/** This file holds all the code related to the storage of SecureSocial
  * LoginTokens in the database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

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

object LoginTokens extends Table[MyToken]("LOGIN_TOKENS") {
  def uuid = column[String]("UUID", O.DBType("TEXT"), O.PrimaryKey)
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def creationTime = column[DateTime]("CREATIONTIME")
  def expirationTime = column[DateTime]("EXPIRATIONTIME")
  def isSignUp = column[Boolean]("ISSIGNUP")
  def isInvitation = column[Boolean]("ISINVITATION")
  def * = uuid ~ email ~ creationTime ~ expirationTime ~ isSignUp ~ isInvitation <> (MyToken.apply _, MyToken.unapply _)
  
  def allInvitations = DB.withSession(implicit session =>
    Query(LoginTokens).filter(_.isInvitation).list )
  
  def ins(myToken: MyToken) = DB.withSession(implicit session =>
    LoginTokens.insert(myToken) )
  
  def del(uuid: String) = DB.withTransaction(implicit session =>
    LoginTokens.filter(_.uuid is uuid).delete )
  
  def withUUID(uuid: String): Option[MyToken] = DB.withSession(implicit session =>
    createFinderBy(_.uuid).apply(uuid).firstOption )

  trait Queries {
    def deleteToken(uuid: String): Unit = del(uuid)
    def findToken(uuid: String): Option[Token] = withUUID(uuid).map(_.toT)

    def save(token: Token): Unit = DB.withTransaction { implicit session =>
      findToken(token.uuid) match {
        case None => LoginTokens.insert(MyToken.fromT(token))
        case Some(t) => LoginTokens.filter(_.uuid is t.uuid).update(MyToken.fromT(token))
      }
    }

    def deleteExpiredTokens: Unit = DB.withTransaction(implicit session =>
      LoginTokens.filter(_.expirationTime <= DateTime.now).delete )
  }
}
