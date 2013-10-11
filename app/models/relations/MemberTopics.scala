package models.relations

import models.entities.{Member, Members, Topics}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/** This file holds all the code related to the storage of Members/Topics
  * interest relation in the database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

case class MemberTopic(
  memberid: Int,
  topicid: Int
)

object MemberTopics extends Table[MemberTopic]("MEMBER_TOPICS") {
  def memberid = column[Int]("MEMBERID")
  def topicid = column[Int]("TOPICID")

  def pk = primaryKey("MEMBERTOPICS_PK", memberid ~ topicid)
  def member = foreignKey("MEMBERTOPICS_MEMBERID_FK", memberid, Members)(_.id)
  def topic = foreignKey("MEMBERTOPICS_TOPIC_FK", topicid, Topics)(_.id)

  def * = memberid ~ topicid <> (MemberTopic, MemberTopic.unapply _)
  
  def all = DB.withSession{implicit session:Session =>
    Query(MemberTopics).list }
  
  def ins(mt: MemberTopic) = DB.withSession{implicit session:Session =>
    MemberTopics.insert(mt) }
  
  def deleteFor(member: Member) = DB.withSession{implicit session:Session =>
    MemberTopics.filter(_.memberid is member.id).delete }

  def insertAll(pts: List[MemberTopic]) = DB.withSession{implicit session:Session =>
    pts.foreach(pt => MemberTopics.insert(pt)) }
}
