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
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def sent = column[DateTime]("SENT")
  def to = column[String]("TO")
  def subject = column[String]("SUBJECT")
  def body = column[String]("BODY")
    
  def * = id ~ sent ~ to ~ subject ~ body <> (SentMail.apply _, SentMail.unapply _)
}