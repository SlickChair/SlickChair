package models

import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import com.github.tototoshi.slick.H2JodaSupport._

case class Id[M <: Model[M]](value: IdType)

trait Model[M <: Model[M]] {
  val metadata: MetaData[M]
  lazy val (id: Id[M], updatedAt, updatedBy) = metadata
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
  metadata: MetaData[Topic],
  name: String,
  description: String
) extends Model[Topic]

case class Person(
  metadata: MetaData[Person],
  firstname: String,
  lastname: String,
  organization: String,
  role: PersonRole,
  email: String
) extends Model[Person]

case class Paper(
  metadata: MetaData[Paper],
  title: String,
  format: PaperType,
  keywords: String,
  abstrct: String,
  nauthors: Int,
  fileid: Option[Id[File]]
) extends Model[Paper]

case class PaperTopic(
  metadata: MetaData[PaperTopic],
  paperid: Id[Paper],
  topicid: Id[Topic]
) extends Model[PaperTopic]

case class Author(
  metadata: MetaData[Author],
  paperid: Id[Paper],
  personid: Id[Person],
  position: Int
) extends Model[Author]

case class File(
  metadata: MetaData[File],
  name: String,
  size: Long,
  content: Array[Byte]
) extends Model[File]

case class Bid(
  metadata: MetaData[Bid],
  paperid: Id[Paper],
  personid: Id[Person],
  value: BidValue
) extends Model[Bid]

case class Assignment(
  metadata: MetaData[Assignment],
  paperid: Id[Paper],
  personid: Id[Person]  
) extends Model[Assignment]

case class Comment(
  metadata: MetaData[Comment],
  paperid: Id[Paper],
  personid: Id[Person],
  content: String
) extends Model[Comment]

case class Review(
  metadata: MetaData[Review],
  paperid: Id[Paper],
  personid: Id[Person],
  confidence: ReviewConfidence,
  evaluation: ReviewEvaluation,
  content: String
) extends Model[Review]

case class Email(
  metadata: MetaData[Email],
  to: String,
  subject: String,
  content: String
) extends Model[Email]
