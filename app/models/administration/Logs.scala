package models.administration

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime

// Logs of when status changes
case class Log(
  id: Int,
  date: DateTime,
  entry: String
)

object Logs extends Table[Log]("LOGS"){
  def id = column[Int ]("id", O.AutoInc)
  def date = column[DateTime]("date")
  def entry = column[String]("entry")

  def pk = primaryKey("logs_pk", id)
  
  def * = id ~ date ~ entry <> (Log.apply _, Log.unapply _)
}