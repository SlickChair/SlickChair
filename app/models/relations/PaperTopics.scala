package models.relations

import models.entities.{Paper, Papers, Topics}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/********/
case class PaperTopic (
  paperid: Int,
  topicid: Int
)

object PaperTopics extends Table[PaperTopic]("PAPER_TOPICS") {
  def paperid = column[Int]("PAPERID")
  def topicid = column[Int]("TOPICID")
  
  def pk = primaryKey("PAPERTOPICS_PK", paperid ~ topicid)
  def paper = foreignKey("PAPERTOPICS_PAPERID_FK", paperid, Papers)(_.id)
  def topic = foreignKey("PAPERTOPICS_TOPICID_FK", topicid, Topics)(_.id)
  def * = paperid ~ topicid <> (PaperTopic, PaperTopic.unapply _)

  def all = DB.withSession(implicit session =>
    Query(PaperTopics).list )
  
  def ins(pt: PaperTopic) = DB.withSession(implicit session =>
    PaperTopics.insert(pt) )
  
  def deleteFor(paper: Paper) = DB.withSession(implicit session =>
    PaperTopics.filter(_.paperid is paper.id).delete )
  
  def insertAll(pts: List[PaperTopic]) = DB.withSession(implicit session =>
    pts.map(pt => PaperTopics.insert(pt)) )
}