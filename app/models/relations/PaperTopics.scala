package models.relations

import models.entities.{Paper, Papers, Topics}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/** This file holds all the code related to the storage of submissions/Topics
  * relation in the database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

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

  def all = DB.withSession{implicit session:Session =>
    Query(PaperTopics).list }
  
  def ins(pt: PaperTopic) = DB.withSession{implicit session:Session =>
    PaperTopics.insert(pt) }
  
  def deleteFor(paper: Paper) = DB.withSession{implicit session:Session =>
    PaperTopics.filter(_.paperid is paper.id).delete }
  
  def insertAll(pts: List[PaperTopic]) = DB.withSession{implicit session:Session =>
    pts.map(pt => PaperTopics.insert(pt)) }
}
