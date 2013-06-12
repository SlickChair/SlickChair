package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models._
import models.entities._

object Bid extends Enumeration with BitmaskedEnumeration {
  type Bid = Value
  val Conflict, Low, Medium, High = Value
}
import Bid._

// Conflicts between submissions and Members
case class MemberBid(
  paperid: Int,
  memberid: Int,
  bid: Bid
)

object MemberBids extends Table[MemberBid]("MEMBER_BIDS") {
  def paperid = column[Int]("PAPERID")
  def memberid = column[Int]("MEMBERID")
  def bid = column[Bid]("BID")
  
  def pk = primaryKey("MEMBERBIDS_PK", paperid ~ memberid)
  def member = foreignKey("MEMBERBIDS_MEMBERID_FK", memberid, Members)(_.id)
  def paper = foreignKey("MEMBERBIDS_PAPERID_FK", paperid, Papers)(_.id)
  
  def * = paperid ~ memberid ~ bid <> (MemberBid.apply _, MemberBid.unapply _)
}