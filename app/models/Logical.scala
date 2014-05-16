package models

import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._

case class Id[M](value: IdType)

trait Model[M] {
  this: M with Product =>
  val metadata: MetaData[M]
  lazy val (id, updatedAt, updatedBy) = metadata
}

trait Entity[M] extends Model[M] {
  this: M with Product =>
  def withId(newId: Id[M]) = (this: M) match {
    case x: Topic => x.copy(metadata=(Id[Topic](newId.value), x.updatedAt, x.updatedBy))
    case x: Person => x.copy(metadata=(Id[Person](newId.value), x.updatedAt, x.updatedBy))
    case x: Paper => x.copy(metadata=(Id[Paper](newId.value), x.updatedAt, x.updatedBy))
    case x: File => x.copy(metadata=(Id[File](newId.value), x.updatedAt, x.updatedBy))
    case x: Comment => x.copy(metadata=(Id[Comment](newId.value), x.updatedAt, x.updatedBy))
  }
}

trait PaperPersonRelation[M <: PaperPersonRelation[M]] extends Model[M] {
  this: M with Product =>
  def paperid: Id[Paper]
  def personid: Id[Person]
}

object PersonRole extends Enumeration with EnumMapper {
  type PersonRole = Value
  val Submitter, Reviewer, Chair = Value
}
import PersonRole._

object PaperType extends Enumeration with EnumMapper {
  type PaperType = Value
  val Full_Paper, Short_Paper, Tool_Demo, Presentation = Value
}
import PaperType._

object ReviewConfidence extends Enumeration with EnumMapper {
  type ReviewConfidence = Value
  val Very_Low, Low, Medium, High, Very_High = Value
}
import ReviewConfidence._

object ReviewEvaluation extends Enumeration with EnumMapper {
  type ReviewEvaluation = Value
  val Strong_Reject, Reject, Neutral, Accept, Strong_Accept = Value
}
import ReviewEvaluation._

object BidValue extends Enumeration with EnumMapper {
  type BidValue = Value
  val Conflict, NoBid, Maybe, YesBid = Value
}
import BidValue._

case class Topic(
  name: String,
  metadata: MetaData[Topic] = noMetaDate
) extends Entity[Topic]

case class Person(
  firstname: String,
  lastname: String,
  organization: String,
  role: PersonRole,
  email: String,
  metadata: MetaData[Person] = noMetaDate
) extends Entity[Person]

case class Paper(
  title: String,
  format: PaperType,
  keywords: String,
  abstrct: String,
  nauthors: Int,
  fileid: Option[Id[File]],
  metadata: MetaData[Paper] = noMetaDate
) extends Entity[Paper]

case class PaperTopic(
  paperid: Id[Paper],
  topicid: Id[Topic],
  metadata: MetaData[PaperTopic] = noMetaDate
) extends Model[PaperTopic]

case class Author(
  paperid: Id[Paper],
  personid: Id[Person],
  position: Int,
  metadata: MetaData[Author] = noMetaDate
) extends PaperPersonRelation[Author]

case class File(
  name: String,
  size: Long,
  content: Array[Byte],
  metadata: MetaData[File] = noMetaDate
) extends Entity[File]

case class Bid(
  paperid: Id[Paper],
  personid: Id[Person],
  value: BidValue,
  metadata: MetaData[Bid] = noMetaDate
) extends PaperPersonRelation[Bid]

case class Assignment(
  paperid: Id[Paper],
  personid: Id[Person]  ,
  metadata: MetaData[Assignment] = noMetaDate
) extends PaperPersonRelation[Assignment]

case class Comment(
  paperid: Id[Paper],
  personid: Id[Person],
  content: String,
  metadata: MetaData[Comment] = noMetaDate
) extends Entity[Comment]

case class Review(
  paperid: Id[Paper],
  personid: Id[Person],
  confidence: ReviewConfidence,
  evaluation: ReviewEvaluation,
  content: String,
  metadata: MetaData[Review] = noMetaDate
) extends PaperPersonRelation[Review]

case class Email(
  to: String,
  subject: String,
  content: String,
  metadata: MetaData[Email] = noMetaDate
) extends Model[Email]
