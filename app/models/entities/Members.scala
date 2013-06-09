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
  val Chair, Member, SubMember = Value
}
import MemberRole._

// Members account info
case class Member(
  id: Int,
  securesocialuserid: String,
  securesocialproviderid: String,
  firstlogindate: DateTime,
  lastlogindate: DateTime,
  role: MemberRole,
  firstname: String,
  lastname: String,
  organization: Option[String],
  positiontitle: Option[String],
  email: String
)

object Members extends Table[Member]("MEMBERS") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def securesocialuserid = column[String]("securesocialuserid")
  def securesocialproviderid = column[String]("securesocialproviderid")
  def firstlogindate = column [DateTime]("firstlogindate")
  def lastlogindate = column [DateTime]("lastlogindate")
  def role = column[MemberRole]("role")
  def firstname = column[String]("firstname")
  def lastname = column[String]("lastname")
  def organization = column[Option[String]]("organization")
  def positiontitle = column[Option[String]]("positiontitle")
  def email = column[String]("email")
  
  def securesocialuser = foreignKey("members_secur  esocialuser_fk", (securesocialuserid, securesocialproviderid), SecureSocialUsers)(u => u.uid ~ u.pid)
  
  def * = id ~ securesocialuserid ~ securesocialproviderid ~ firstlogindate ~ lastlogindate ~ role ~ firstname ~ lastname ~ organization ~ positiontitle ~ email <> (Member.apply _, Member.unapply _)
} 