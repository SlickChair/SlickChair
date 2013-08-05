package models.entities

import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport.dateTimeTypeMapper
import PaperType.enumTypeMapper
import models.BitmaskedEnumeration
import models.utils.Files
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/********/
object PaperType extends Enumeration with BitmaskedEnumeration {
  type PaperType = Value
  val Full_Paper, Short_Paper, Tool_Demo, Presentation = Value
}
import PaperType._

case class Paper(
  id: Int,
  contactemail: String,
  submissiondate: DateTime,
  lastupdate: DateTime,
  accepted: Option[Boolean],
  title: String,
  format: PaperType,
  keywords: String,
  abstrct: String,
  fileid: Option[Int]
)
case class NewPaper(contactemail: String, submissiondate: DateTime, lastupdate: DateTime, accepted: Option[Boolean], title: String, format: PaperType, keywords: String, abstrct: String, fileid: Option[Int])

object Papers extends Table[Paper]("PAPERS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def contactemail = column[String]("CONTACTEMAIL", O.DBType("TEXT"))
  def submissiondate = column[DateTime]("SUBMISSIONDATE")
  def lastupdate = column[DateTime]("LASTUPDATE")
  def accepted = column[Option[Boolean]]("ACCEPTED")
  def title = column[String]("TITLE", O.DBType("TEXT"))
  def format = column[PaperType]("FORMAT")
  def keywords = column[String]("KEYWORDS", O.DBType("TEXT"))
  def abstrct = column[String]("ABSTRCT", O.DBType("TEXT"))
  def fileid = column[Option[Int]]("FILEID")
  
  def file = foreignKey("PAPERS_FILEID_FK", fileid, Files)(_.id)
  def * =  id ~ contactemail ~ submissiondate ~ lastupdate ~ accepted ~ title ~ format ~ keywords ~ abstrct ~ fileid <> (Paper, Paper.unapply _)
  def autoInc = contactemail ~ submissiondate ~ lastupdate ~ accepted ~ title ~ format ~ keywords ~ abstrct ~ fileid <> (NewPaper, NewPaper.unapply _) returning id

  def all: List[Paper] = DB.withSession(implicit session =>
    Query(Papers).list )
  
  def ins(newPaper: NewPaper): Int = DB.withSession(implicit session =>
    Papers.autoInc.insert(newPaper) )
  
  def updt(paper: Paper) = DB.withSession(implicit session =>
    Papers.filter(_.id is paper.id).update(paper) )
  
  def withId(paperId: Int): Option[Paper] = DB.withSession(implicit session =>
    Query(Papers).filter(_.id is paperId).list.headOption )

  def withEmail(email: String): Option[Paper] = DB.withSession(implicit session =>
    Query(Papers).filter(_.contactemail is email).list.headOption )
  
  def relevantCategories: List[(String, String)] = DB.withSession(implicit session =>
    List(
      ("All Authors", Query(Papers).map(_.contactemail).list)
    ).map(c => (c._1, c._2.mkString(", ")))
  )
}