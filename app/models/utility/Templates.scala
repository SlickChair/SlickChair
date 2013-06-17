
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
  def name = column[String]("NAME", O.DBType("TEXT"))
  def subject = column[String]("SUBJECT", O.DBType("TEXT"))
  def body = column[String]("BODY", O.DBType("TEXT"))
  
  def * = id ~ name ~ subject ~ body <> (Template, Template.unapply _)
}