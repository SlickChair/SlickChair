package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import java.sql.Date
import org.joda.time.DateTime
import models._
import models.utils._
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
  fileid: Option[Int]
)
// Paper without the id field
case class NewPaper(contactemail: String, submissiondate: DateTime, lastupdate: DateTime, accepted: Option[Boolean], title: String, format: PaperFormat, keywords: String, abstrct: String, fileid: Option[Int])

object Papers extends Table[Paper]("PAPERS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def contactemail = column[String]("CONTACTEMAIL", O.DBType("TEXT"))
  def submissiondate = column[DateTime]("SUBMISSIONDATE")
  def lastupdate = column[DateTime]("LASTUPDATE")
  def accepted = column[Option[Boolean]]("ACCEPTED")
  def title = column[String]("TITLE", O.DBType("TEXT"))
  def format = column[PaperFormat]("FORMAT")
  def keywords = column[String]("KEYWORDS", O.DBType("TEXT"))
  def abstrct = column[String]("ABSTRCT", O.DBType("TEXT"))
  def fileid = column[Option[Int]]("FILEID")
  
  def file = foreignKey("PAPERS_FILEID_FK", fileid, Files)(_.id)
  def * = id ~ contactemail ~ submissiondate ~ lastupdate ~ accepted ~ title ~ format ~ keywords ~ abstrct ~ fileid <> (Paper, Paper.unapply _)
  def autoInc = contactemail ~ submissiondate ~ lastupdate ~ accepted ~ title ~ format ~ keywords ~ abstrct ~ fileid <> (NewPaper, NewPaper.unapply _) returning id

  def all: List[Paper] = DB.withSession { implicit session =>
    Query(Papers).list }
  
  def ins(newPaper: NewPaper): Int = DB.withSession { implicit session =>
    Papers.autoInc.insert(newPaper) }
  
  def updt(paper: Paper) = DB.withSession { implicit session =>
    Papers.filter(_.id is paper.id).update(paper) }
  
  def withId(paperId: Int): Option[Paper] = DB.withSession { implicit session =>
    Query(Papers).filter(_.id is paperId).list.headOption }

  def withEmail(email: String): Option[Paper] = DB.withSession { implicit session =>
    Query(Papers).filter(_.contactemail is email).list.headOption }
  
  def relevantCategories: List[(String, String)] = DB.withSession { implicit session =>
    List(
      ("All Authors", Query(Papers).map(_.contactemail).list)
    ).map(c => (c._1, c._2.mkString(", ")))
  }
}