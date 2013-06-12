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
  id: Option[Int],
  contactemail: String//,
  // submissiondate: DateTime,
  // lastupdate: DateTime,
  // accepted: Option[Boolean],
  // title: String,
  // format: PaperFormat,
  // keywords: String,
  // abstrct: String,
  // fileid: Option[Int]
)

object Papers extends Table[Paper]("PAPERS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def contactemail = column[String]("CONTACTEMAIL")
  // def submissiondate = column[DateTime]("SUBMISSIONDATE")
  // def lastupdate = column[DateTime]("LASTUPDATE")
  // def accepted = column[Option[Boolean]]("ACCEPTED")
  // def title = column[String]("TITLE")
  // def format = column[PaperFormat]("FORMAT")
  // def keywords = column[String]("KEYWORDS")
  // def abstrct = column[String]("ABSTRCT")
  // def fileid = column[Option[Int]]("FILEID")
  
  // def paper = foreignKey("PAPERS_FILEID_FK", fileid, Files)(_.id)
  def * = id.? ~ contactemail/* ~ submissiondate ~ lastupdate ~ accepted ~ title ~ format ~ keywords ~ abstrct ~ fileid*/ <> (Paper.apply _, Paper.unapply _)
  // def autoInc = * returning id

  // def all = DB.withSession(implicit session =>
  //   Query(Papers).list)
  
  // def insert(paper: Paper) = DB.withSession(implicit session =>
  //   Papers.autoInc.insert(paper))
}