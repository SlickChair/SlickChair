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
  def * = paperid ~ topicid <> (PaperTopic, PaperTopic.unapply _)

  def all = DB.withSession { implicit session =>
    Query(PaperTopics).list }
  
  def of(id: Int) = DB.withSession { implicit session =>
    Query(PaperTopics).filter(_.paperid is id).list }

  def ins(pt: PaperTopic) = DB.withSession { implicit session =>
    PaperTopics.insert(pt) }
  
  def deleteFor(paper: Paper) = DB.withSession { implicit session =>
    PaperTopics.filter(_.paperid is paper.id).mutate(_.delete) }
  
  def createAll(pts: List[PaperTopic]) = DB.withSession { implicit session =>
    pts.map(pt => PaperTopics.insert(pt)) }
    
  def ofPaper(paper: Paper): List[Topic] = DB.withSession { implicit session =>
    (for(p <- PaperTopics; t <- Topics if t.id is p.topicid) yield t).list }
    // PaperTopics.flatMap(p => Query(Topics).filter(_.id is p.topicid)).list }
}