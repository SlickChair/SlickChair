package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime
import models.entities._

// Members comments on submissions
case class Comment(
  id: Int,
  paperid: Int,
  memberid: Int,
  submissiondate: DateTime,
  lastupdate: DateTime,
  content: String
)

object Comments extends Table[Comment]("COMMENTS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def paperid = column[Int]("PAPERID")
  def memberid = column[Int]("MEMBERID")
  def submissiondate = column[DateTime]("SUBMISSIONDATE")
  def lastupdate = column[DateTime]("LASTUPDATE")
  def content = column[String]("CONTENT")

  def paper = foreignKey("COMMENTS_PAPERID_FK", paperid, Papers)(_.id)
  def member = foreignKey("COMMENTS_MEMBERID_FK", memberid, Members)(_.id)

  def * = id ~ paperid ~ memberid ~ submissiondate ~ lastupdate ~ content <> (Comment.apply _, Comment.unapply _)
}