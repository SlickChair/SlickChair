package models

import org.joda.time.DateTime
import play.api.templates.Html

object PersonRole extends Enumeration with EnumMapper {
  type PersonRole = Value
  val Submitter, Reviewer, Chair = Value
}
import PersonRole._

object BidValue extends Enumeration with EnumMapper {
  type BidValue = Value
  val Conflict, NoBid, Maybe, YesBid = Value
}
import BidValue._

object PaperType extends Enumeration with EnumMapper {
  type PaperType = Value
  val Full_paper, Short_paper, Tool_demo, Presentation = Value
}
import PaperType._

object ReviewConfidence extends Enumeration with EnumMapper {
  type ReviewConfidence = Value
  val Expert, High, Medium, Low, None_ = Value
}
import ReviewConfidence._

object ReviewEvaluation extends Enumeration with EnumMapper {
  type ReviewEvaluation = Value
  val Strong_accept, Accept, Weak_accept, Weak_reject, Reject, Strong_reject = Value
}
import ReviewEvaluation._

case class Topic(
  name: String,
  metadata: Metadata[Topic] = newMetadata
) extends Model[Topic]

case class Person(
  firstname: String,
  lastname: String,
  organization: String,
  email: String,
  metadata: Metadata[Person] = newMetadata
) extends Model[Person] {
  override val id = pk(email)
  def name = Html(s"""<span title="$email"> ${firstname.capitalize} ${lastname.capitalize}</span>""")
}

case class Role(
  personid: Id[Person],
  value: PersonRole,
  metadata: Metadata[Role] = newMetadata
) extends Model[Role] {
  override val id = pk(personid)
}

case class Paper(
  title: String,
  format: PaperType,
  keywords: String,
  abstrct: String,
  nauthors: Int,
  fileid: Option[Id[File]],
  metadata: Metadata[Paper] = newMetadata
) extends Model[Paper]

case class PaperIndex(
  paperid: Id[Paper],
  metadata: Metadata[PaperIndex] = newMetadata
) extends Model[PaperIndex] {
  override val id = pk(paperid)
}

case class PaperTopic(
  paperid: Id[Paper],
  topicid: Id[Topic],
  metadata: Metadata[PaperTopic] = newMetadata
) extends Model[PaperTopic] {
  override val id = pk(paperid, topicid)
}

case class Author(
  paperid: Id[Paper],
  personid: Id[Person],
  position: Int,
  metadata: Metadata[Author] = newMetadata
) extends Model[Author] {
  override val id = pk(paperid, personid)
}

case class File(
  name: String,
  size: Long,
  content: Array[Byte],
  metadata: Metadata[File] = newMetadata
) extends Model[File]

case class Bid(
  paperid: Id[Paper],
  personid: Id[Person],
  value: BidValue,
  metadata: Metadata[Bid] = newMetadata
) extends Model[Bid] {
  override val id = pk(paperid, personid)
}

case class Assignment(
  paperid: Id[Paper],
  personid: Id[Person],
  metadata: Metadata[Assignment] = newMetadata
) extends Model[Assignment] {
  override val id = pk(paperid, personid)
}

case class Comment(
  paperid: Id[Paper],
  personid: Id[Person],
  content: String,
  metadata: Metadata[Comment] = newMetadata
) extends Model[Comment]

case class Review(
  paperid: Id[Paper],
  personid: Id[Person],
  confidence: ReviewConfidence,
  evaluation: ReviewEvaluation,
  content: String,
  metadata: Metadata[Review] = newMetadata
) extends Model[Review] {
  override val id = pk(paperid, personid)
}

case class Email(
  to: String,
  subject: String,
  content: String,
  metadata: Metadata[Email] = newMetadata
) extends Model[Email]
