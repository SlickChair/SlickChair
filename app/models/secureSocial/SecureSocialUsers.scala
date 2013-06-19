package models.secureSocial

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import securesocial.core._

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
    UserId(uid, pid), s"$firstName $lastName", firstName, lastName, Some(email), None, AuthenticationMethod(authMethod),
    None, None, password.map(p => PasswordInfo(hasher.getOrElse(""), p, salt))
  )
}
object User {
  // IMPORTANT NOTE: At this point (i.email.get) we assume that the provider
  // gives us an email, which is not the case for some of them (eg twitter).
  def fromIdentity(i: Identity) = User(
    i.id.id, i.id.providerId, i.email.get, firstName = i.firstName,
    i.lastName, i.authMethod.method, i.passwordInfo.map(_.hasher),
    i.passwordInfo.map(_.password), i.passwordInfo.map(_.salt).getOrElse(None)
  )
}

object SecureSocialUsers extends Table[User]("SECURE_SOCIAL_USERS") {
  def uid = column[String]("UID", O.DBType("TEXT"))
  def pid = column[String]("PID", O.DBType("TEXT"))
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def firstName = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastName = column[String]("LASTNAME", O.DBType("TEXT"))
  def authMethod = column[String]("AUTHMETHOD", O.DBType("TEXT"))
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
  
  def withEmail(email: String) = DB.withTransaction { implicit session =>
    Query(SecureSocialUsers).filter(_.email is email).list.headOption
  }

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
