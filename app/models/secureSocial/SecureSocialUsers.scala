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
    avatarUrl: Option[String],
    authMethod: String,
    hasher: Option[String],
    password: Option[String],
    salt: Option[String]
    ) {
  def id: UserId = UserId(uid, pid)
  def toIdentity: Identity = SocialUser(
    id = UserId(this.uid, this.pid),
    email = Some(this.email),
    firstName = this.firstName,
    lastName = this.lastName,
    fullName = this.firstName + " " + this.lastName,
    avatarUrl = this.avatarUrl,
    authMethod = AuthenticationMethod(this.authMethod),
    oAuth1Info = None,
    oAuth2Info = None,
    passwordInfo = this.password.map( p =>
      Some(PasswordInfo(this.hasher.getOrElse(""), p, this.salt))
    ).getOrElse(None)
  )
}
object User {
  def fromIdentity(i: Identity) = User(
    uid = i.id.id,
    pid = i.id.providerId,
    email = i.email.get, // TODO: might fail, throw login error
    firstName = if (!i.firstName.isEmpty) i.firstName else i.fullName.split(' ').head,
    lastName = if (!i.lastName.isEmpty) i.lastName else i.fullName.split(' ').tail.head,
    avatarUrl = i.avatarUrl,
    authMethod = i.authMethod.method,
    hasher = i.passwordInfo.map(_.hasher),
    password = i.passwordInfo.map(_.password),
    salt = i.passwordInfo.map(_.salt).getOrElse(None)
  )
}

object SecureSocialUsers extends Table[User]("SECURE_SOCIAL_USERS") {
  def uid = column[String]("uid")
  def pid = column[String]("pid")
  def email = column[String]("email")
  def firstName = column[String]("firstname")
  def lastName = column[String]("lastname")
  def avatarurl = column[Option[String]]("avatarurl")
  def authMethod = column[String]("authmethod")
  def hasher = column[Option[String]]("hasher")
  def password = column[Option[String]]("password")
  def salt = column[Option[String]]("salt")
  
  def pk = primaryKey("securesocialusers_pk", uid ~ pid)
  
  def * = uid ~ pid ~ email ~ firstName ~ lastName ~ avatarurl ~ authMethod ~ hasher ~ password ~ salt <> (User.apply _, User.unapply _)

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