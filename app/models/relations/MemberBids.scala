package models.relations

import Bid.enumTypeMapper
import models.BitmaskedEnumeration
import models.entities.{Member, Members, Papers}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/** This file holds all the code related to the storage of Bids on Paper
  * submissions in the database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

object Bid extends Enumeration with BitmaskedEnumeration {
  type Bid = Value
  val Conflict, Low, Medium, High = Value
}
import Bid._

case class MemberBid (
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
  
  def * = paperid ~ memberid ~ bid <> (MemberBid, MemberBid.unapply _)
  
  def all = DB.withSession{implicit session:Session =>
    Query(MemberBids).list }
  
  def ins(mb: MemberBid) = DB.withSession{implicit session:Session =>
    MemberBids.insert(mb) }
  
  def of(member: Member) = DB.withSession{implicit session:Session =>
    Query(MemberBids).filter(_.memberid is member.id).list }
  
  def deleteFor(member: Member) = DB.withSession{implicit session:Session =>
    MemberBids.filter(_.memberid is member.id).delete }

  def insertAll(memberBids: List[MemberBid]) = DB.withSession{implicit session:Session =>
    memberBids.foreach(MemberBids.insert) }
}
