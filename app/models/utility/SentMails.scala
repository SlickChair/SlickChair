package models.utility

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime

// Emails sent out
case class SentMail(
  id: Int,
  sent: DateTime,
  to: String,
  subject: String,
  body: String
)

object SentMails extends Table[SentMail]("SENTMAILS") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def sent = column[DateTime]("sent")
  def to = column[String]("to")
  def subject = column[String]("subject")
  def body = column[String]("body")
    
  def * = id ~ sent ~ to ~ subject ~ body <> (SentMail.apply _, SentMail.unapply _)
}