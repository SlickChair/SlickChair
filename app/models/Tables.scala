package models

import BidValue.BidValue
import Mappers.idSlickMapper
import PaperType.PaperType
import Role.Role
import ReviewConfidence.ReviewConfidence
import ReviewEvaluation.ReviewEvaluation
import play.api.db.slick.Config.driver.simple._

class TopicTable(tag: Tag) extends Table[Topic](tag, "TOPIC") with RepoTable[Topic] {
  def name = column[String]("NAME", O.DBType("TEXT"))
  def * = (name, (id, updatedAt, updatedBy)) <> (Topic.tupled, Topic.unapply)
}

class PersonTable(tag: Tag) extends Table[Person](tag, "PERSON") with RepoTable[Person] {
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  def organization = column[String]("ORGANIZATION")
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def * = (firstname, lastname, organization, email, (id, updatedAt, updatedBy)) <> (Person.tupled, Person.unapply)
}

class PersonRoleTable(tag: Tag) extends Table[PersonRole](tag, "ROLE") with RepoTable[PersonRole] {
  def personid = column[Id[Person]]("PERSONID")
  def value = column[Role]("VALUE")
  def * = (personid, value, (id, updatedAt, updatedBy)) <> (PersonRole.tupled, PersonRole.unapply)
}

class PaperTable(tag: Tag) extends Table[Paper](tag, "PAPER") with RepoTable[Paper] {
  def title = column[String]("TITLE", O.DBType("TEXT"))
  def format = column[PaperType]("FORMAT")
  def keywords = column[String]("KEYWORDS", O.DBType("TEXT"))
  def abstrct = column[String]("ABSTRCT", O.DBType("TEXT"))
  def nauthors = column[Int]("NAUTHORS")
  def fileid = column[Option[Id[File]]]("FILE")
  def * = (title, format, keywords, abstrct, nauthors, fileid, (id, updatedAt, updatedBy)) <> (Paper.tupled, Paper.unapply)
}

class PaperIndexTable(tag: Tag) extends Table[PaperIndex](tag, "PAPERINDEX") with RepoTable[PaperIndex] {
  def paperid = column[Id[Paper]]("PAPERID")
  def * = (paperid, (id, updatedAt, updatedBy)) <> (PaperIndex.tupled, PaperIndex.unapply)
}

class PaperTopicTable(tag: Tag) extends Table[PaperTopic](tag, "PAPERTOPIC") with RepoTable[PaperTopic] {
  def paperid = column[Id[Paper]]("PAPERID")
  def topicid = column[Id[Topic]]("TOPICID")
  def * = (paperid, topicid, (id, updatedAt, updatedBy)) <> (PaperTopic.tupled, PaperTopic.unapply)
}

class PaperAuthorTable(tag: Tag) extends Table[PaperAuthor](tag, "PAPERAUTHOR") with RepoTable[PaperAuthor] {
  def paperid = column[Id[Paper]]("PAPERID")
  def personid = column[Id[Person]]("PERSONID")
  def position = column[Int]("POSITION")
  def * = (paperid, personid, position, (id, updatedAt, updatedBy)) <> (PaperAuthor.tupled, PaperAuthor.unapply)
}

class CommentTable(tag: Tag) extends Table[Comment](tag, "COMMENT") with RepoTable[Comment] {
  def paperid = column[Id[Paper]]("PAPERID")
  def personid = column[Id[Person]]("PERSONID")
  def content = column[String]("CONTENT", O.DBType("TEXT"))
  def * = (paperid, personid, content, (id, updatedAt, updatedBy)) <> (Comment.tupled, Comment.unapply)
}

class ReviewTable(tag: Tag) extends Table[Review](tag, "REVIEW") with RepoTable[Review] {
  def paperid = column[Id[Paper]]("PAPERID")
  def personid = column[Id[Person]]("PERSONID")
  def confidence = column[ReviewConfidence]("CONFIDENCE")
  def evaluation = column[ReviewEvaluation]("EVALUATION")
  def content = column[String]("CONTENT", O.DBType("TEXT"))
  def * = (paperid, personid, confidence, evaluation, content, (id, updatedAt, updatedBy)) <> (Review.tupled, Review.unapply)
}

class FileTable(tag: Tag) extends Table[File](tag, "FILE") with RepoTable[File] {
  def name = column[String]("NAME", O.DBType("TEXT"))
  def size = column[Long]("SIZE")
  def content = column[Array[Byte]]("CONTENT")
  def * = (name, size, content, (id, updatedAt, updatedBy)) <> (File.tupled, File.unapply)
}

class EmailTable(tag: Tag) extends Table[Email](tag, "EMAIL") with RepoTable[Email] {
  def to = column[String]("TO", O.DBType("TEXT"))
  def subject = column[String]("SUBJECT", O.DBType("TEXT"))
  def content = column[String]("CONTENT", O.DBType("TEXT"))
  def * = (to, subject, content, (id, updatedAt, updatedBy)) <> (Email.tupled, Email.unapply)
}

class BidTable(tag: Tag) extends Table[Bid](tag, "BID") with RepoTable[Bid] {
  def paperid = column[Id[Paper]]("PAPERID")
  def personid = column[Id[Person]]("PERSONID")
  def value = column[BidValue]("VALUE")
  def * = (paperid, personid, value, (id, updatedAt, updatedBy)) <> (Bid.tupled, Bid.unapply)
}

class AssignmentTable(tag: Tag) extends Table[Assignment](tag, "ASSIGNMENT") with RepoTable[Assignment] {
  def paperid = column[Id[Paper]]("PAPERID")
  def personid = column[Id[Person]]("PERSONID")
  def value = column[Boolean]("VALUE")
  def * = (paperid, personid, value, (id, updatedAt, updatedBy)) <> (Assignment.tupled, Assignment.unapply)
}
