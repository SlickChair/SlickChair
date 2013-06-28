package models.entities

import models.relations.{MemberTopics, PaperTopics}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

case class Topic(
  id: Int,
  name: String,
  description: String
)
case class NewTopic(
  name: String,
  description: String
)

object Topics extends Table[Topic]("TOPICS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME", O.DBType("TEXT"))
  def description = column[String]("DESCRIPTION", O.DBType("TEXT"))

  def * =  id ~ name ~ description <> (Topic, Topic.unapply _)
  def autoInc = name ~ description <> (NewTopic, NewTopic.unapply _) returning id

  def all = DB.withSession(implicit session =>
    Query(Topics).list )
  
  def ins(newTopic: NewTopic) = DB.withSession(implicit session =>
    Topics.autoInc.insert(newTopic) )
  
  def withId(topicId: Int) = DB.withSession(implicit session =>
    Topics.filter(_.id is topicId).list.headOption )
  
  def of(paper: Paper) = DB.withSession(implicit session =>
    (for(p <- PaperTopics; t <- Topics if t.id is p.topicid) yield t).list )
  
  def of(member: Member) = DB.withSession(implicit session =>
    (for(m <- MemberTopics; t <- Topics if t.id is m.topicid) yield t).list )
}