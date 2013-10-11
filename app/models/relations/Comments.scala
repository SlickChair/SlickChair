package models.relations

import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport.dateTimeTypeMapper
import models.entities.{Members, Paper, Papers}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/** This file holds all the code related to the storage of Review Comments in
  * the database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

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
  
  def ins(newComment: NewComment) = DB.withSession{implicit session:Session =>
    Comments.autoinc.insert(newComment) }
  
  def ofPaper(paper: Paper): List[Comment] = DB.withSession{implicit session:Session =>
    Query(Comments).filter(_.paperid is paper.id).list }
}
