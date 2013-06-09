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
  def memberid = column[Int]("memberid")
  def topicid = column[Int]("topicid")

  def pk = primaryKey("membertopics_pk", memberid ~ topicid)
  def member = foreignKey("membertopics_memberid_fk", memberid, Members)(_.id)
  def topic = foreignKey("membertopics_topic_fk", topicid, Topics)(_.id)

  def * = memberid ~ topicid <> (MemberTopic.apply _, MemberTopic.unapply _)
}