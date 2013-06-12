package models.secureSocial

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import securesocial.core._
import play.api.Logger

case class User(
    uid: String,
    pid: String,
    email: String,
    firstName: String,
    lastName: String,
    authMethod: String,
    hasher: Option[String],
    password: Option[String],
    salt: Option[String]
    ) {
  def id: UserId = UserId(uid, pid)
  def toIdentity: Identity = SocialUser(
    UserId(uid, pid), Some(email), firstName, lastName, s"$firstName $lastName", None, AuthenticationMethod(authMethod),
    None, None, password.map(p => Some(PasswordInfo(hasher.getOrElse(""), p, salt)))
  )
}
object User {
  def fromIdentity(i: Identity) = User( // email.get will fail for some providers, eg twitter
    i.id.id, i.id.providerId, i.email.get, firstName = i.firstName, i.lastName, i.authMethod.method,
    i.passwordInfo.map(_.hasher), i.passwordInfo.map(_.password), i.passwordInfo.map(_.salt)
  )
}

object SecureSocialUsers extends Table[User]("SECURE_SOCIAL_USERS") {
  def uid = column[String]("UID")
  def pid = column[String]("PID")
  def email = column[String]("EMAIL")
  def firstName = column[String]("FIRSTNAME")
  def lastName = column[String]("LASTNAME")
  def authMethod = column[String]("AUTHMETHOD")
  def hasher = column[Option[String]]("HASHER")
  def password = column[Option[String]]("PASSWORD")
  def salt = column[Option[String]]("SALT")
  
  def pk = primaryKey("SECURESOCIALUSERS_PK", uid ~ pid)
  def * = uid ~ pid ~ email ~ firstName ~ lastName ~ authMethod ~ hasher ~ password ~ salt <> (User.apply _, User.unapply _)

  def userByUserId(userId: UserId) = 
    Query(SecureSocialUsers).filter( user =>
      (user.uid is userId.id) && (user.pid is userId.providerId) )
  
  def userByEmailAndProvider(email: String, pid: String) = 
    Query(SecureSocialUsers).filter( user =>
      (user.email is email) && (user.pid is pid) )
  
  trait Queries {
    def save(identity: Identity): Identity = DB.withTransaction { implicit session =>
      val user = User.fromIdentity(identity)
      find(user.id) match {
        case None =>
          SecureSocialUsers.insert(user)
        case Some(u) =>
          userByUserId(u.id).update(user)
      }
      identity
    }
    
    def find(userId: UserId): Option[Identity] = DB.withSession { implicit session =>
      userByUserId(userId).firstOption.map(_.toIdentity)
    }
    
    def findByEmailAndProvider(email: String, pid: String): Option[Identity] = DB.withSession { implicit session =>
      userByEmailAndProvider(email, pid).firstOption.map(_.toIdentity)
    }
  }
}
