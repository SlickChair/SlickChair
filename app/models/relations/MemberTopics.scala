package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models.entities._

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
  
  def all = DB.withSession(implicit session =>
    Query(MemberTopics).list )
  
  def ins(mt: MemberTopic) = DB.withSession(implicit session =>
    MemberTopics.insert(mt) )
  
  def deleteFor(member: Member) = DB.withSession(implicit session =>
    MemberTopics.filter(_.memberid is member.id).delete )

  def insertAll(pts: List[MemberTopic]) = DB.withSession(implicit session =>
    pts.foreach(pt => MemberTopics.insert(pt)) )
}