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

object PaperTopics extends Table[PaperTopic]("PAPER_TOPICS"){
  def paperid = column[Int]("paperid")
  def topicid = column[Int]("topicid")
  
  def pk = primaryKey("papertopics_pk", (paperid, topicid))
  def paper = foreignKey("paperid_fk", paperid, Papers)(_.id)
  def topic = foreignKey("topicid_fk", topicid, Topics)(_.id)

  def * = paperid ~ topicid <> (PaperTopic.apply _, PaperTopic.unapply _)
}