package models.utility

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Email templates
case class Template(
  id: Int,
  name: String,
  subject: String,
  body: String
)

object Templates extends Table[Template]("TEMPLATES") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME")
  def subject = column[String]("SUBJECT")
  def body = column[String]("BODY")
  
  def * = id ~ name ~ subject ~ body <> (Template.apply _, Template.unapply _)
}