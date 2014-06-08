package models

import Mappers.EnumSlickMapper
import play.api.templates.Html

object Role extends Enumeration with EnumSlickMapper {
  type Role = Value
  val Author, PC_Member, Chair = Value
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

object Confidence extends Enumeration with EnumSlickMapper {
  type Confidence = Value
  val Expert, High, Medium, Low, None_ = Value
}

object Evaluation extends Enumeration with EnumSlickMapper {
  type Evaluation = Value
  val Strong_reject, Reject, Weak_reject, Weak_accept, Accept, Strong_accept = Value
}

object Decision extends Enumeration with EnumSlickMapper {
  type Decision = Value
  val Rejected, Temporary_rejected, Undecided, Temporary_accepted, Accepted = Value
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
  personId: Id[Person],
  value: Role.Role,
  metadata: Metadata[PersonRole] = newMetadata
) extends Model[PersonRole] {
  override val id = pk(personId)
}

case class Paper(
  title: String,
  format: PaperType.PaperType,
  keywords: String,
  abstrct: String,
  nAuthors: Int,
  fileId: Option[Id[File]],
  withdrawn: Boolean,
  metadata: Metadata[Paper] = newMetadata
) extends Model[Paper]

case class PaperIndex(
  paperId: Id[Paper],
  metadata: Metadata[PaperIndex] = newMetadata
) extends Model[PaperIndex] {
  override val id = pk(paperId)
}

case class PaperAuthor(
  paperId: Id[Paper],
  personId: Id[Person],
  position: Int,
  metadata: Metadata[PaperAuthor] = newMetadata
) extends Model[PaperAuthor] {
  override val id = pk(paperId, personId)
}

case class PaperDecision(
  paperId: Id[Paper],
  value: Decision.Decision,
  metadata: Metadata[PaperDecision] = newMetadata
) extends Model[PaperDecision] {
  override val id = pk(paperId)
}

case class File(
  name: String,
  size: Long,
  content: Array[Byte],
  metadata: Metadata[File] = newMetadata
) extends Model[File]

case class Bid(
  paperId: Id[Paper],
  personId: Id[Person],
  value: BidValue.BidValue,
  metadata: Metadata[Bid] = newMetadata
) extends Model[Bid] {
  override val id = pk(paperId, personId)
}

case class Assignment(
  paperId: Id[Paper],
  personId: Id[Person],
  value: Boolean,
  metadata: Metadata[Assignment] = newMetadata
) extends Model[Assignment] {
  override val id = pk(paperId, personId)
}

case class Comment(
  paperId: Id[Paper],
  personId: Id[Person],
  content: String,
  metadata: Metadata[Comment] = newMetadata
) extends Model[Comment]

case class Review(
  paperId: Id[Paper],
  personId: Id[Person],
  confidence: Confidence.Confidence,
  evaluation: Evaluation.Evaluation,
  content: String,
  metadata: Metadata[Review] = newMetadata
) extends Model[Review] {
  override val id = pk(paperId, personId)
}

case class Email(
  to: String,
  subject: String,
  content: String,
  metadata: Metadata[Email] = newMetadata
) extends Model[Email]

case class Configuration(
  name: String,
  chairRoles: Boolean = false,
  chairAssignment: Boolean = false,
  chairDecision: Boolean = false,
  chairSql: Boolean = true,
  pcmemberBid: Boolean = false,
  pcmemberReview: Boolean = false,
  pcmemberComment: Boolean = false,
  authorNewSubmission: Boolean = false,
  authorEditSubmission: Boolean = false,
  metadata: Metadata[Configuration] = newMetadata
) extends Model[Configuration]
