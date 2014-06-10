package models

import BidValue.BidValue
import Mappers.idSlickMapper
import PaperType.PaperType
import Role.Role
import Decision.Decision
import Confidence.Confidence
import Evaluation.Evaluation
import play.api.db.slick.Config.driver.simple._

class PersonTable(tag: Tag) extends Table[Person](tag, "PERSON") with RepoTable[Person] {
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  def organization = column[String]("ORGANIZATION")
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def * = (firstname, lastname, organization, email, (id, updatedAt, updatedBy)) <> (Person.tupled, Person.unapply)
}

class PersonRoleTable(tag: Tag) extends Table[PersonRole](tag, "ROLE") with RepoTable[PersonRole] {
  def personId = column[Id[Person]]("PERSONID")
  def value = column[Role]("VALUE")
  def * = (personId, value, (id, updatedAt, updatedBy)) <> (PersonRole.tupled, PersonRole.unapply)
}

class PaperTable(tag: Tag) extends Table[Paper](tag, "PAPER") with RepoTable[Paper] {
  def title = column[String]("TITLE", O.DBType("TEXT"))
  def format = column[PaperType]("FORMAT")
  def keywords = column[String]("KEYWORDS", O.DBType("TEXT"))
  def abstrct = column[String]("ABSTRCT", O.DBType("TEXT"))
  def nAuthors = column[Int]("nAuthors")
  def fileId = column[Option[Id[File]]]("FILE")
  def withdrawn = column[Boolean]("WITHDRAWN")
  def * = (title, format, keywords, abstrct, nAuthors, fileId, withdrawn, (id, updatedAt, updatedBy)) <> (Paper.tupled, Paper.unapply)
}

class PaperIndexTable(tag: Tag) extends Table[PaperIndex](tag, "PAPERINDEX") with RepoTable[PaperIndex] {
  def paperId = column[Id[Paper]]("PAPERID")
  def * = (paperId, (id, updatedAt, updatedBy)) <> (PaperIndex.tupled, PaperIndex.unapply)
}

class PaperAuthorTable(tag: Tag) extends Table[PaperAuthor](tag, "PAPERAUTHOR") with RepoTable[PaperAuthor] {
  def paperId = column[Id[Paper]]("PAPERID")
  def personId = column[Id[Person]]("PERSONID")
  def position = column[Int]("POSITION")
  def * = (paperId, personId, position, (id, updatedAt, updatedBy)) <> (PaperAuthor.tupled, PaperAuthor.unapply)
}

class PaperDecisionTable(tag: Tag) extends Table[PaperDecision](tag, "PAPERDECISION") with RepoTable[PaperDecision] {
  def paperId = column[Id[Paper]]("PAPERID")
  def value = column[Decision]("VALUE")
  def * = (paperId, value, (id, updatedAt, updatedBy)) <> (PaperDecision.tupled, PaperDecision.unapply)
}

class CommentTable(tag: Tag) extends Table[Comment](tag, "COMMENT") with RepoTable[Comment] {
  def paperId = column[Id[Paper]]("PAPERID")
  def personId = column[Id[Person]]("PERSONID")
  def content = column[String]("CONTENT", O.DBType("TEXT"))
  def * = (paperId, personId, content, (id, updatedAt, updatedBy)) <> (Comment.tupled, Comment.unapply)
}

class ReviewTable(tag: Tag) extends Table[Review](tag, "REVIEW") with RepoTable[Review] {
  def paperId = column[Id[Paper]]("PAPERID")
  def personId = column[Id[Person]]("PERSONID")
  def confidence = column[Confidence]("CONFIDENCE")
  def evaluation = column[Evaluation]("EVALUATION")
  def content = column[String]("CONTENT", O.DBType("TEXT"))
  def * = (paperId, personId, confidence, evaluation, content, (id, updatedAt, updatedBy)) <> (Review.tupled, Review.unapply)
}

class FileTable(tag: Tag) extends Table[File](tag, "FILE") with RepoTable[File] {
  def name = column[String]("NAME", O.DBType("TEXT"))
  def size = column[Long]("SIZE")
  def content = column[Array[Byte]]("CONTENT")
  def * = (name, size, content, (id, updatedAt, updatedBy)) <> (File.tupled, File.unapply)
}

class EmailTable(tag: Tag) extends Table[Email](tag, "EMAIL") with RepoTable[Email] {
  def recipients = column[String]("RECIPIENTS", O.DBType("TEXT"))
  def subject = column[String]("SUBJECT", O.DBType("TEXT"))
  def content = column[String]("CONTENT", O.DBType("TEXT"))
  def * = (recipients, subject, content, (id, updatedAt, updatedBy)) <> (Email.tupled, Email.unapply)
}

class BidTable(tag: Tag) extends Table[Bid](tag, "BID") with RepoTable[Bid] {
  def paperId = column[Id[Paper]]("PAPERID")
  def personId = column[Id[Person]]("PERSONID")
  def value = column[BidValue]("VALUE")
  def * = (paperId, personId, value, (id, updatedAt, updatedBy)) <> (Bid.tupled, Bid.unapply)
}

class AssignmentTable(tag: Tag) extends Table[Assignment](tag, "ASSIGNMENT") with RepoTable[Assignment] {
  def paperId = column[Id[Paper]]("PAPERID")
  def personId = column[Id[Person]]("PERSONID")
  def value = column[Boolean]("VALUE")
  def * = (paperId, personId, value, (id, updatedAt, updatedBy)) <> (Assignment.tupled, Assignment.unapply)
}

class ConfigurationTable(tag: Tag) extends Table[Configuration](tag, "CONFIGURATION") with RepoTable[Configuration] {
  def name = column[String]("NAME", O.DBType("TEXT"))
  def chairCanChangeRoles = column[Boolean]("CHAIRROLES")
  def chairCanAssignSubmissions = column[Boolean]("CHAIRASSIGNMENT")
  def chairCanDecideOnAcceptance = column[Boolean]("CHAIRDECISION")
  def chairCanRunSqlQueries = column[Boolean]("CHAIRSQL")
  def pcmemberCanBid = column[Boolean]("PCMEMBERBID")
  def pcmemberCanReview = column[Boolean]("PCMEMBERREVIEW")
  def pcmemberCanComment = column[Boolean]("PCMEMBERCOMMENT")
  def authorCanMakeNewSubmissions = column[Boolean]("AUTHORNEWSUBMISSION")
  def authorCanEditSubmissions = column[Boolean]("AUTHOREDITSUBMISSION")
  def authorCanSeeReviews = column[Boolean]("AUTHORSEEREVIEWS")
  def showListOfAcceptedPapers = column[Boolean]("SHOWLISTOFACCEPTEDPAPER")
  def * = (name, chairCanChangeRoles, chairCanAssignSubmissions, chairCanDecideOnAcceptance, chairCanRunSqlQueries, pcmemberCanBid, pcmemberCanReview, pcmemberCanComment, authorCanMakeNewSubmissions, authorCanEditSubmissions, authorCanSeeReviews, showListOfAcceptedPapers, (id, updatedAt, updatedBy)) <> (Configuration.tupled, Configuration.unapply)
}
