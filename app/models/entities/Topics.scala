package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Topics
case class Topic(
  id: Option[Int],
  name: String,
  description: String
)

object Topics extends Table[Topic]("TOPICS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME")
  def description = column[String]("DESCRIPTION")

  def * = id.? ~ name ~ description <> (Topic.apply _, Topic.unapply _)
  def autoInc = * returning id

  def all = DB.withSession(implicit session =>
    Query(Topics).list)
  
  def insert(topic: Topic) = DB.withSession(implicit session =>
    Topics.autoInc.insert(topic))
}