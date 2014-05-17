package models

import org.joda.time.DateTime

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
  metadata: Metadata[Topic] = noMetadata
) extends Model[Topic]

case class Person(
  firstname: String,
  lastname: String,
  organization: String,
  email: String,
  metadata: Metadata[Person] = noMetadata
) extends Model[Person] {
  override val id = idFromString(email)
}

case class Role(
  personid: Id[Person],
  value: PersonRole,
  metadata: Metadata[Role] = noMetadata
) extends Model[Role] {
  override val id = idFromId(personid)
}

case class Paper(
  title: String,
  format: PaperType,
  keywords: String,
  abstrct: String,
  nauthors: Int,
  fileid: Option[Id[File]],
  metadata: Metadata[Paper] = noMetadata
) extends Model[Paper]

case class PaperTopic(
  paperid: Id[Paper],
  topicid: Id[Topic],
  metadata: Metadata[PaperTopic] = noMetadata
) extends Model[PaperTopic] {
  override val id = idFromIds(paperid, topicid)
}

case class Author(
  paperid: Id[Paper],
  personid: Id[Person],
  position: Int,
  metadata: Metadata[Author] = noMetadata
) extends Model[Author] {
  override val id = idFromIds(paperid, personid)
}

case class File(
  name: String,
  size: Long,
  content: Array[Byte],
  metadata: Metadata[File] = noMetadata
) extends Model[File]

case class Bid(
  paperid: Id[Paper],
  personid: Id[Person],
  value: BidValue,
  metadata: Metadata[Bid] = noMetadata
) extends Model[Bid] {
  override val id = idFromIds(paperid, personid)
}

case class Assignment(
  paperid: Id[Paper],
  personid: Id[Person],
  metadata: Metadata[Assignment] = noMetadata
) extends Model[Assignment] {
  override val id = idFromIds(paperid, personid)
}

case class Comment(
  paperid: Id[Paper],
  personid: Id[Person],
  content: String,
  metadata: Metadata[Comment] = noMetadata
) extends Model[Comment]

case class Review(
  paperid: Id[Paper],
  personid: Id[Person],
  confidence: ReviewConfidence,
  evaluation: ReviewEvaluation,
  content: String,
  metadata: Metadata[Review] = noMetadata
) extends Model[Review] {
  override val id = idFromIds(paperid, personid)
}

case class Email(
  to: String,
  subject: String,
  content: String,
  metadata: Metadata[Email] = noMetadata
) extends Model[Email]
