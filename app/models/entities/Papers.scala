package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime
import models._
import models.secureSocial._

object PaperFormat extends Enumeration with BitmaskedEnumeration {
  type PaperFormat = Value
  val Standard, Shorts, ToolDemo, Poster = Value
}
import PaperFormat._

// Submission data
case class Paper(
  id: Int,
  contactemail: String,
  submissiondate: DateTime,
  lastupdate: DateTime,
  accepted: Option[Boolean],
  title: String,
  format: PaperFormat,
  keywords: String,
  abstrct: String,
  data: Option[Array[Byte]]
)

object Papers extends Table[Paper]("PAPERS") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def contactemail = column[String]("contactemail")
  def submissiondate = column[DateTime]("submissiondate")
  def lastupdate = column[DateTime]("lastupdate")
  def accepted = column[Option[Boolean]]("accepted")
  def title = column[String]("title")
  def format = column[PaperFormat]("format")
  def keywords = column[String]("keywords")
  def abstrct = column[String]("abstrct")
  def data = column[Option[Array[Byte]]]("data")

  def * = id ~ contactemail ~ submissiondate ~ lastupdate ~ accepted ~ title ~ format ~ keywords ~ abstrct ~ data <> (Paper.apply _, Paper.unapply _)
  def autoInc = * returning id
  
  def insert(paper: Paper) = DB.withSession { implicit session =>
    Papers.autoInc.insert(paper)
  }
}