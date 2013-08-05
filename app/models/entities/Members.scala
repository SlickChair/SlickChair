package models.entities

import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport.dateTimeTypeMapper
import MemberRole.enumTypeMapper
import models.BitmaskedEnumeration
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import securesocial.core.SecuredRequest

/** Enumerate the possible roles of a member. */
object MemberRole extends Enumeration with BitmaskedEnumeration {
  type MemberRole = Value
  val Chair, Member, Disabled = Value
}
import MemberRole._

/** A Member of the SlickChair system. Instances of this class are stored in
  * the database and returned when executing queries.
  * 
  * @constructor  Create a new Member.
  * @param  id  the unique Member identifier
  * @param  email  the Member email adresse
  * @param  invitedas  the email adresse used to send the invitation 
  * @param  firstlogindate  the time at which the Member first logged-in
  * @param  lastlogindate  the time at which the Member last logged-in
  * @param  role  the role of this Member
  * @param  firstname  the first name of the Member
  * @param  lastname  the last name of the Member
  */
case class Member(
  id: Int,
  email: String,
  invitedas: String,
  firstlogindate: DateTime,
  lastlogindate: DateTime,
  role: MemberRole,
  firstname: String,
  lastname: String
)

/** A Member without id field. This class is to be used when first inserting
  * Member into the database, which will automaticaly assign it an
  * autoincremented id.
  *
  * @See Member
  */
case class NewMember(
  email: String,
  invitedas: String,
  firstlogindate: DateTime,
  lastlogindate: DateTime,
  role: MemberRole,
  firstname: String,
  lastname: String
)


/** Represents a database table storing Member objects. This singleton follows
  * the Slick recomendations and provides both definition of the table in
  * therm of columns name/types and primary/foreign key, as well as a set of
  * methods to manipulate the table for inserting, deleting and querying.
  */
object Members extends Table[Member]("MEMBERS") {
  /** "Defines a {} {} of type {} with name {} as a {}.".format("id", "column", "Int", "ID", "autoincementing primary key") */ 
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def invitedas = column[String]("INVITEDAS", O.DBType("TEXT"))
  def firstlogindate = column[DateTime]("FIRSTLOGINDATE")
  def lastlogindate = column[DateTime]("LASTLOGINDATE")
  def role = column[MemberRole]("ROLE")
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  
  /** The star projection informs Slick on how to (@see slick doc...) */
  def * =  id ~ email ~ invitedas ~ firstlogindate ~ lastlogindate ~ role ~ firstname ~ lastname <> (Member, Member.unapply _)
  def autoinc = email ~ invitedas ~ firstlogindate ~ lastlogindate ~ role ~ firstname ~ lastname <> (NewMember, NewMember.unapply _) returning id
  
  def ins(newMember: NewMember) = DB.withSession(implicit session =>
    Members.autoinc.insert(newMember) )
  
  def all: List[Member] = DB.withSession(implicit session =>
    Query(Members).list )
  
  def withId(memberId: Int): Option[Member] = DB.withSession(implicit session =>
    Query(Members).filter(_.id is memberId).list.headOption )
  
  def withEmail(memberEmail: String): Option[Member] = DB.withSession(implicit session =>
    Query(Members).filter(_.email is memberEmail).list.headOption )
  
  def relevantCategories: List[(String, String)] = DB.withSession(implicit session =>
    List(
      ("All Members", Query(Members).map(_.email).list)
    ).map(c => (c._1, c._2.mkString(", ")))
  )
  
  def promote(memberId: Int, newRole: MemberRole) = DB.withSession(implicit session =>
    Query(Members).filter(_.id is memberId).map(_.role).update(newRole) )
  
  /** The last get won't fail if called after the MemberOrChair authentication succeeded. */
  def getFromRequest[T](implicit request: SecuredRequest[T]) =
    Members.withEmail(request.user.email.get).get
}