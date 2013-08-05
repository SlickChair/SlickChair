package models.utils

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import java.sql.Date
import org.joda.time.DateTime

/********/
case class Email(
  id: Int,
  to: String,
  subject: String,
  body: String,
  sent: DateTime
)
case class NewEmail(to: String, subject: String, body: String, sent: DateTime)

object SentEmails extends Table[Email]("SENT_EMAILS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def to = column[String]("TO", O.DBType("TEXT"))
  def subject = column[String]("SUBJECT", O.DBType("TEXT"))
  def body = column[String]("BODY", O.DBType("TEXT"))
  def sent = column[DateTime]("SENT")
    
  def * = id ~ to ~ subject ~ body ~ sent <> (Email, Email.unapply _)
  def autoinc = to ~ subject ~ body ~ sent <> (NewEmail, NewEmail.unapply _) returning id
  
  def ins(newEmail: NewEmail) = DB.withSession(implicit session =>
    SentEmails.autoinc.insert(newEmail) )
}