package models.utility

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Settings
case class Setting(
  name: String,
  value: String
)

object Settings extends Table[Setting]("SETTINGS") {
  def name = column[String]("name", O.PrimaryKey)
  def value = column[String]("value")

  def * = name ~ value <> (Setting.apply _, Setting.unapply _)
}