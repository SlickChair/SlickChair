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
  def name = column[String]("NAME", O.DBType("TEXT"), O.PrimaryKey)
  def value = column[String]("VALUE", O.DBType("TEXT"))

  def * = name ~ value <> (Setting, Setting.unapply _)
}