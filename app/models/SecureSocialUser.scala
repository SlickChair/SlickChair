package models

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import securesocial.core._

case class User(
    uId: String,
    providerId: String,
    email: Option[String],
    firstName: String,
    lastName: String,
    authMethod: String,
    hasher: Option[String],
    password: Option[String],
    salt: Option[String]
    ) {
  def id: UserId = UserId(uId, providerId)
  def fullName: String = firstName + " " + lastName
  def toIdentity: Identity = SocialUser(
    id = UserId(this.uId, this.providerId),
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
    fullName = this.fullName,
    avatarUrl = None,
    authMethod = AuthenticationMethod(this.authMethod),
    oAuth1Info = None,
    oAuth2Info = None,
    passwordInfo = this.password.map( p =>
      Some(PasswordInfo(this.hasher.getOrElse(""), p, this.salt))
    ).getOrElse(None)
  )
}
object User {
  def fromIdentity(iden: Identity) = User(
    uId = iden.id.id,
    providerId = iden.id.providerId,
    email = iden.email,
    firstName = if (!iden.firstName.isEmpty) iden.firstName else iden.fullName.split(' ').head,
    lastName = if (!iden.lastName.isEmpty) iden.lastName else iden.fullName.split(' ').tail.head,
    authMethod = iden.authMethod.method,
    hasher = iden.passwordInfo.map(_.hasher),
    password = iden.passwordInfo.map(_.password),
    salt = iden.passwordInfo.map(_.salt).getOrElse(None)
  )
}

object SecureSocialUsers extends Table[User]("SECURE_SOCIAL_USERS") {
  def uId = column[String]("U_ID")
  def providerId = column[String]("PROVIDER_ID")
  def email = column[Option[String]]("EMAIL")
  def firstName = column[String]("FIRST_NAME")
  def lastName = column[String]("LAST_NAME")
  def authMethod = column[String]("AUTH_METHOD")
  def hasher = column[Option[String]]("HASHER")
  def password = column[Option[String]]("PASSWORD")
  def salt = column[Option[String]]("SALT")
  // def pk = primaryKey("PK_USERS", (k1, k2))
  def * = uId ~ providerId ~ email ~ firstName ~ lastName ~ authMethod ~ hasher ~ password ~ salt <> (User.apply _, User.unapply _)

  def userByUserId(userId: UserId) = 
    Query(SecureSocialUsers).filter( user =>
      (user.uId is userId.id) && (user.providerId is userId.providerId) )
  
  def userByEmailAndProvider(email: String, providerId: String) = 
    Query(SecureSocialUsers).filter( user =>
      (user.email is email) && (user.providerId is providerId) )
  
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
    
    def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = DB.withSession { implicit session =>
      userByEmailAndProvider(email, providerId).firstOption.map(_.toIdentity)
    }
  }
}