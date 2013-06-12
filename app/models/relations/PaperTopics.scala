package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models.entities._

// Author suggested submission/topic pairings
case class PaperTopic(
  paperid: Int,
  topicid: Int
)

object PaperTopics extends Table[PaperTopic]("PAPER_TOPICS") {
  def paperid = column[Int]("PAPERID")
  def topicid = column[Int]("TOPICID")
  
  def pk = primaryKey("PAPERTOPICS_PK", paperid ~ topicid)
  def paper = foreignKey("PAPERTOPICS_PAPERID_FK", paperid, Papers)(_.id)
  def topic = foreignKey("PAPERTOPICS_TOPICID_FK", topicid, Topics)(_.id)

  def * = paperid ~ topicid <> (PaperTopic.apply _, PaperTopic.unapply _)
}