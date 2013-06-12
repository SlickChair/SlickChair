package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models.entities._

// Member topics of interest
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

  def * = memberid ~ topicid <> (MemberTopic.apply _, MemberTopic.unapply _)
}