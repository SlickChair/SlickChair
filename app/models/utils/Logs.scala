package models.utils

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import java.sql.Date
import org.joda.time.DateTime

/********/
case class Log(
  id: Int,
  date: DateTime,
  entry: String
)

object Logs extends Table[Log]("LOGS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def date = column[DateTime]("DATE")
  def entry = column[String]("ENTRY", O.DBType("TEXT"))

  def * = id ~ date ~ entry <> (Log, Log.unapply _)
}