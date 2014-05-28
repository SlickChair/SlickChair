package models

import Mappers.EnumSlickMapper
import play.api.templates.Html

object Role extends Enumeration with EnumSlickMapper {
  type Role = Value
  val Author, Reviewer, Chair = Value
}

object BidValue extends Enumeration with EnumSlickMapper {
  type BidValue = Value
  val Conflict, NoBid, Maybe, YesBid = Value
}

object PaperType extends Enumeration with EnumSlickMapper {
  type PaperType = Value
  val Student_paper = Value
  // val Full_paper, Short_paper, Tool_demo, Presentation = Value
}

object ReviewConfidence extends Enumeration with EnumSlickMapper {
  type ReviewConfidence = Value
  val Expert, High, Medium, Low, None_ = Value
}

object ReviewEvaluation extends Enumeration with EnumSlickMapper {
  type ReviewEvaluation = Value
  val Strong_accept, Accept, Weak_accept, Weak_reject, Reject, Strong_reject = Value
}

object Decision extends Enumeration with EnumSlickMapper {
  type Decision = Value
  val Reject, Temporary_reject, Undecided, Temporary_accept, Accept = Value
}

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

case class PersonRole(
  personid: Id[Person],
  value: Role.Role,
  metadata: Metadata[PersonRole] = newMetadata
) extends Model[PersonRole] {
  override val id = pk(personid)
}

case class Paper(
  title: String,
  format: PaperType.PaperType,
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
case class PaperAuthor(
  paperid: Id[Paper],
  personid: Id[Person],
  position: Int,
  metadata: Metadata[PaperAuthor] = newMetadata
) extends Model[PaperAuthor] {
  override val id = pk(paperid, personid)
}

case class PaperDecision(
  paperid: Id[Paper],
  value: Decision.Decision,
  metadata: Metadata[PaperDecision] = newMetadata
) extends Model[PaperDecision] {
  override val id = pk(paperid)
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
  value: BidValue.BidValue,
  metadata: Metadata[Bid] = newMetadata
) extends Model[Bid] {
  override val id = pk(paperid, personid)
}

case class Assignment(
  paperid: Id[Paper],
  personid: Id[Person],
  value: Boolean,
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
  confidence: ReviewConfidence.ReviewConfidence,
  evaluation: ReviewEvaluation.ReviewEvaluation,
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
