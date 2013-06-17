package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime
import models._
import models.secureSocial._

object MemberRole extends Enumeration with BitmaskedEnumeration {
  type MemberRole = Value
  val Chair, Member = Value
}
import MemberRole._

// Members account info
case class Member(
  id: Int,
  email: String,
  firstlogindate: DateTime,
  lastlogindate: DateTime,
  role: MemberRole,
  firstname: String,
  lastname: String,
  organization: String
)
case class NewMember(email: String, firstlogindate: DateTime, lastlogindate: DateTime, role: MemberRole, firstname: String, lastname: String, organization: String)

object Members extends Table[Member]("MEMBERS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def firstlogindate = column [DateTime]("FIRSTLOGINDATE")
  def lastlogindate = column [DateTime]("LASTLOGINDATE")
  def role = column[MemberRole]("ROLE")
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  def organization = column[String]("ORGANIZATION", O.DBType("TEXT"))
  
  def * = id ~ email ~ firstlogindate ~ lastlogindate ~ role ~ firstname ~ lastname ~ organization <> (Member, Member.unapply _)
  def autoinc = email ~ firstlogindate ~ lastlogindate ~ role ~ firstname ~ lastname ~ organization <> (NewMember, NewMember.unapply _) returning id
  
  def ins(newMember: NewMember) = DB.withSession { implicit session =>
    Members.autoinc.insert(newMember) }
  
  def withId(memberId: Int): Option[Member] = DB.withSession { implicit session =>
    Query(Members).filter(_.id is memberId).list.headOption }

  def withEmail(memberEmail: String): Option[Member] = DB.withSession { implicit session =>
    Query(Members).filter(_.email is memberEmail).list.headOption }
} 