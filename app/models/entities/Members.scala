package models.entities

import org.joda.time.DateTime

import com.github.tototoshi.slick.JodaSupport.dateTimeTypeMapper

import MemberRole.enumTypeMapper
import models.BitmaskedEnumeration
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import securesocial.core.SecuredRequest

object MemberRole extends Enumeration with BitmaskedEnumeration {
  type MemberRole = Value
  val Chair, Member, Disabled = Value
}
import MemberRole._

// Members account info
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
case class NewMember(email: String, invitedas: String, firstlogindate: DateTime, lastlogindate: DateTime, role: MemberRole, firstname: String, lastname: String)

object Members extends Table[Member]("MEMBERS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def invitedas = column[String]("INVITEDAS", O.DBType("TEXT"))
  def firstlogindate = column [DateTime]("FIRSTLOGINDATE")
  def lastlogindate = column [DateTime]("LASTLOGINDATE")
  def role = column[MemberRole]("ROLE")
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  
  def * = id ~ email ~ invitedas ~ firstlogindate ~ lastlogindate ~ role ~ firstname ~ lastname <> (Member, Member.unapply _)
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