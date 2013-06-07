package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime
import models.entities._

// Reviewers comments on submissions
case class Comment(
  id: Int,
  paperid: Int,
  reviewerid: Int,
  submissiondate: DateTime,
  lastupdate: DateTime,
  content: String
)

object Comments extends Table[Comment]("COMMENTS"){
  def id = column[Int]("id", O.AutoInc)
  def paperid = column[Int]("paperid")
  def reviewerid = column[Int]("reviewerid")
  def submissiondate = column[DateTime]("submissiondate")
  def lastupdate = column[DateTime]("lastupdate")
  def content = column[String]("content")

  def pk = primaryKey("comments_pk", id)
  def paper = foreignKey("paperid_fk", paperid, Papers)(_.id)
  def reviewer = foreignKey("reviewerid_fk", reviewerid, Reviewers)(_.id)

  def * = id ~ paperid ~ reviewerid ~ submissiondate ~ lastupdate ~ content <> (Comment.apply _, Comment.unapply _)
}