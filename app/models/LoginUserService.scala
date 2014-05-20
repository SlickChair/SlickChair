package models

import scala.slick.lifted.ProvenShape.proveShapeOf
import org.joda.time.DateTime
import models.Mappers.dateTimeSlickMapper
import play.api.Application
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import securesocial.core._
import securesocial.core.providers.Token

 /** Implements the UserServicePlugin required to use the SecureSocial plugin on
   * top of Slick. See http://securesocial.ws/guide/user-service.html */
class LoginUserService(application: Application) extends UserServicePlugin(application)
    with LoginUsers.Queries with LoginTokens.Queries {
  def saveHook(user: User)(implicit s: Session): Unit = {
    import models._
    import PersonRole._
    Connection(s) insert List(Person(user.firstname, user.lastname, "", user.email))
    ()
  }
}

case class User(
  uid: String,
  pid: String,
  email: String,
  firstname: String,
  lastname: String,
  authmethod: String,
  hasher: Option[String],
  password: Option[String],
  salt: Option[String]
) {
  def id: IdentityId = IdentityId(uid, pid)
  def toIdentity: SocialUser = SocialUser(
    identityId=IdentityId(uid, pid),
    firstName=firstname,
    lastName=lastname,
    fullName=s"$firstname $lastname",
    email=Some(email),
    avatarUrl=None,
    authMethod=AuthenticationMethod(authmethod),
    oAuth1Info=None,
    oAuth2Info=None,
    passwordInfo= password.map(p => PasswordInfo(hasher.getOrElse(""), p, salt))
  )
}
object User {
  // IMPORTANT: At this point (i.email.get) we assume that the provider gives
  // us an email, which is not the case for some of them (e.g. Twitter).
  def fromIdentity(i: Identity) = User(
    uid=i.identityId.userId,
    pid=i.identityId.providerId,
    email=i.email.get,
    firstname=i.firstName,
    lastname=i.lastName,
    authmethod=i.authMethod.method,
    hasher=i.passwordInfo.map(_.hasher),
    password=i.passwordInfo.map(_.password),
    salt=i.passwordInfo.map(_.salt).getOrElse(None)
  )
}

class LoginUserTable(tag: Tag) extends Table[User](tag, "LOGINUSERS") {
  def uid = column[String]("UID", O.DBType("TEXT"))
  def pid = column[String]("PID", O.DBType("TEXT"))
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  def authmethod = column[String]("AUTHMETHOD", O.DBType("TEXT"))
  def hasher = column[Option[String]]("HASHER", O.DBType("TEXT"))
  def password = column[Option[String]]("PASSWORD", O.DBType("TEXT"))
  def salt = column[Option[String]]("SALT", O.DBType("TEXT"))
  
  def pk = primaryKey("LOGINUSERS_PK", (uid, pid))
  def * = (uid, pid, email, firstname, lastname, authmethod, hasher, password, salt) <> ((User.apply _).tupled, User.unapply)
}

object LoginUsers extends TableQuery(new LoginUserTable(_)) {
  def UserByidentityId(identityId: IdentityId)(implicit s: Session) = 
    this.filter(user => (user.uid is identityId.userId) && (user.pid is identityId.providerId))
  
  def userByEmailAndProvider(email: String, pid: String)(implicit s: Session) = 
    this.filter(user => (user.email is email) && (user.pid is pid))
  
  def withEmail(email: String)(implicit s: Session) =
    this.filter(_.email is email).list.headOption

  trait Queries {
    def saveHook(user: User)(implicit s: Session): Unit
    
    def save(identity: Identity): Identity = {
      DB withTransaction { implicit s: Session =>
        val user = User.fromIdentity(identity)
        saveHook(user)
        find(user.id) match {
          case None =>
            LoginUsers.insert(user)
          case Some(u) =>
            UserByidentityId(u.identityId).update(user)
        }
        user.toIdentity
      }
    }
    
    def find(identityId: IdentityId): Option[Identity] = {
      DB withSession { implicit s: Session =>
        UserByidentityId(identityId).firstOption.map(_.toIdentity)
      }
    }
    
    def findByEmailAndProvider(email: String, pid: String): Option[Identity] = {
      DB withSession { implicit s: Session =>
        userByEmailAndProvider(email, pid).firstOption.map(_.toIdentity)
      }
    }
  }
}

case class MyToken(
  uuid: String,
  email: String,
  creationTime: DateTime,
  expirationTime: DateTime,
  isSignUp: Boolean,
  isInvitation: Boolean
) {
  def toT = Token(uuid, email, creationTime, expirationTime, isSignUp)
}
object MyToken {
  def fromT(t: Token) = MyToken(t.uuid, t.email, t.creationTime, t.expirationTime, t.isSignUp, false)
}

class LoginTokenTable(tag: Tag) extends Table[MyToken](tag, "LOGINTOKENS") {
  def uuid = column[String]("UUID", O.DBType("text"), O.PrimaryKey)
  def email = column[String]("EMAIL", O.DBType("text"))
  def creationTime = column[DateTime]("CREATIONTIME")
  def expirationTime = column[DateTime]("EXPIRATIONTIME")
  def isSignUp = column[Boolean]("ISSIGNUP")
  def isInvitation = column[Boolean]("ISINVITATION")
  def * = (uuid, email, creationTime, expirationTime, isSignUp, isInvitation) <> ((MyToken.apply _).tupled, MyToken.unapply)
}

object LoginTokens extends TableQuery(new LoginTokenTable(_)) {
  def allInvitations(implicit s: Session) =
    this.filter(_.isInvitation).list
  
  def ins(myToken: MyToken)(implicit s: Session) =
    LoginTokens.insert(myToken)
  
  def del(uuid: String)(implicit s: Session) =
    this.filter(_.uuid is uuid).delete
  
  def withIdType(uuid: String)(implicit s: Session): Option[MyToken] =
    this.filter(_.uuid is uuid).firstOption 

  trait Queries {
    def deleteToken(uuid: String): Unit = {
      DB withSession { implicit s: Session =>
        del(uuid)
      }; ()
    }
    
    def findToken(uuid: String): Option[Token] = {
      DB withSession { implicit s: Session =>
        withIdType(uuid).map(_.toT)
      }
    }

    def save(token: Token): Unit = {
      DB withTransaction { implicit s: Session =>
        findToken(token.uuid) match {
          case None => LoginTokens.insert(MyToken.fromT(token))
          case Some(t) => LoginTokens.filter(_.uuid is t.uuid).update(MyToken.fromT(token))
        }
      }; ()
    }

    def deleteExpiredTokens(): Unit = {
      DB withSession { implicit s: Session =>
        LoginTokens.filter(_.expirationTime <= DateTime.now).delete
      }; ()
    }
  }
}
