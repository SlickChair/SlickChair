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
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def paperid = column[Int]("paperid")
  def memberid = column[Int]("memberid")
  def submissiondate = column[DateTime]("submissiondate")
  def lastupdate = column[DateTime]("lastupdate")
  def content = column[String]("content")

  def paper = foreignKey("comments_paperid_fk", paperid, Papers)(_.id)
  def member = foreignKey("comments_memberid_fk", memberid, Members)(_.id)

  def * = id ~ paperid ~ memberid ~ submissiondate ~ lastupdate ~ content <> (Comment.apply _, Comment.unapply _)
}