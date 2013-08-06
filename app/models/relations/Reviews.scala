package models.relations

import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport.dateTimeTypeMapper
import models.BitmaskedEnumeration
import models.entities.{Member, Members, Paper, Papers}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/** This file holds all the code related to the storage of Reviews in the
  * database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

object ReviewConfidence extends Enumeration with BitmaskedEnumeration {
  type ReviewConfidence = Value
  val VeryLow, Low, Medium, High, VeryHigh = Value
}
import ReviewConfidence._

object ReviewEvaluation extends Enumeration with BitmaskedEnumeration {
  type ReviewEvaluation = Value
  val StrongReject, Reject, Neutral, Accept, StrongAccept = Value
}
import ReviewEvaluation._

// Submission/Member assignments and review data
case class Review(
  paperid: Int,
  memberid: Int,
  submissiondate: Option[DateTime],
  lastupdate: Option[DateTime],
  confidence: ReviewConfidence,
  evaluation: ReviewEvaluation,
  content: String
)

object Reviews extends Table[Review]("REVIEWS") {
  def paperid = column[Int]("PAPERID")
  def memberid = column[Int]("MEMBERID")
  def submissiondate = column[Option[DateTime]]("SUBMISSIONDATE")
  def lastupdate = column[Option[DateTime]]("LASTUPDATE")
  def confidence = column[ReviewConfidence]("CONFIDENCE")
  def evaluation = column[ReviewEvaluation]("EVALUATION")
  def content = column[String]("CONTENT", O.DBType("TEXT"))
  
  def pk = primaryKey("MEMBERS_PK", paperid ~ memberid)
  def member = foreignKey("MEMBERS_MEMBERID_FK", memberid, Members)(_.id)
  def paper = foreignKey("MEMBERS_PAPERID_FK", paperid, Papers)(_.id)

  def * = paperid ~ memberid ~ submissiondate ~ lastupdate ~ confidence ~ evaluation ~ content <> (Review.apply _, Review.unapply _)
  
  def ins(review: Review) = DB.withSession(implicit session =>
    Reviews.insert(review) )
  
  def updt(review: Review) = DB.withSession(implicit session =>
    Reviews.filter(r => r.paperid === review.paperid && r.memberid === review.memberid).update(review) )
  
  def of(paper: Paper, member: Member): Option[Review] = DB.withSession(implicit session =>
    Query(Reviews).filter(r => r.paperid === paper.id && r.memberid === member.id).list.headOption )
  
  def ofPaper(paper: Paper): List[Review] = DB.withSession(implicit session =>
    Query(Reviews).filter(_.paperid is paper.id).list )
}