package models.relations

import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport.dateTimeTypeMapper
import models.entities.{Members, Paper, Papers}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/********/
case class Comment(
  id: Int,
  paperid: Int,
  memberid: Int,
  submissiondate: DateTime,
  content: String
)
case class NewComment(paperid: Int, memberid: Int, submissiondate: DateTime, content: String)

object Comments extends Table[Comment]("COMMENTS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def paperid = column[Int]("PAPERID")
  def memberid = column[Int]("MEMBERID")
  def submissiondate = column[DateTime]("SUBMISSIONDATE")
  def content = column[String]("CONTENT", O.DBType("TEXT"))

  def paper = foreignKey("COMMENTS_PAPERID_FK", paperid, Papers)(_.id)
  def member = foreignKey("COMMENTS_MEMBERID_FK", memberid, Members)(_.id)

  def * = id ~ paperid ~ memberid ~ submissiondate ~ content <> (Comment, Comment.unapply _)
  def autoinc = paperid ~ memberid ~ submissiondate ~ content <> (NewComment, NewComment.unapply _) returning id
  
  def ins(newComment: NewComment) = DB.withSession(implicit session =>
    Comments.autoinc.insert(newComment) )
  
  def ofPaper(paper: Paper): List[Comment] = DB.withSession(implicit session =>
    Query(Comments).filter(_.paperid is paper.id).list )
}