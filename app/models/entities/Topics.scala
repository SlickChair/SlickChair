package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Topics
case class Topic(
  id: Int,
  name: String,
  description: Option[String]
)

object Topics extends Table[Topic]("TOPICS") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[Option[String]]("description")
  
  def * = id ~ name ~ description <> (Topic.apply _, Topic.unapply _)
} 