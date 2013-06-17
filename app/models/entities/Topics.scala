package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Topics
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

  def * = id ~ name ~ description <> (Topic, Topic.unapply _)
  def autoInc = name ~ description <> (NewTopic, NewTopic.unapply _) returning id

  def all = DB.withSession { implicit session =>
    Query(Topics).list }
  
  def ins(newTopic: NewTopic) = DB.withSession { implicit session =>
    Topics.autoInc.insert(newTopic) }
  
  def withId(topicId: Int) = DB.withSession { implicit session =>
    Topics.filter(_.id is topicId).list.headOption }
}