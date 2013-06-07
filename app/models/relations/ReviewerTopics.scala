package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models.entities._

// Reviewer topics of interest
case class ReviewerTopic(
  reviewerid: Int,
  topicid: Int
)

object ReviewerTopics extends Table[ReviewerTopic]("REVIEWER_TOPICS"){
  def reviewerid = column[Int]("reviewerid")
  def topicid = column[Int]("topicid")

  def pk = primaryKey("reviewertopics_pk", (reviewerid, topicid))
  def reviewer = foreignKey("reviewerid_fk", reviewerid, Papers)(_.id)
  def topic = foreignKey("topic_fk", topicid, Topics)(_.id)

  def * = reviewerid ~ topicid <> (ReviewerTopic.apply _, ReviewerTopic.unapply _)
}