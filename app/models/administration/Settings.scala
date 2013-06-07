package models.administration

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Settings
case class Setting(
  name: String,
  value: String
)

object Settings extends Table[Setting]("SETTINGS"){
  def name = column[String]("name")
  def value = column[String]("value")

  def pk = primaryKey("settings_pk", name)

  def * = name ~ value <> (Setting.apply _, Setting.unapply _)
}