package models

import BidValue._
import Decision._
import Mappers.idSlickMapper
import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import Role._
import scala.language.postfixOps
import play.api.mvc.Call

case class Query(db: Database) {
  implicit val session: Session = db.session
  import db._
  
  def roleOf(id: Id[Person]): Role =
    personRoles.filter(_.personId is id).first.value
  def papersOf(id: Id[Person]): List[Paper] =
    paperAuthors filter (_.personId is id) flatMap { a => papers.filter(_.id is a.paperId) } list
  def indexOf(id: Id[Paper]): Int =
    (paperIndices sortBy (_.updatedAt) map (_.paperId) list).indexOf(id) + 1
  def prevNextSubmission(id: Id[Paper], f: Id[Paper] => Call): (Option[Call], Option[Call]) = {
    val indices = allPaperIndices.map(_.paperId).zipWithIndex
    val iOf: Id[Paper] => Int = paperId => indices.find(_._1 == paperId).get._2 
    val sortedPapers = allPapers map (_.id) sortBy iOf
    ((None :: sortedPapers.map(Some(_)) ::: List(None)).iterator
      sliding 3
      find { _(1) == Some(id) }
      map { x => (x(0) map f, x(2) map f) }).get
  }
  def authorsOf(id: Id[Paper]): List[Person] = {
    paperAuthors filter (_.paperId is id) sortBy (_.position) take (paperWithId(id).nAuthors) flatMap { a => persons filter (_.id is a.personId) } list
  }
  def commentsOn(id: Id[Paper]): List[Comment] =
    comments filter (_.paperId is id) list
  def reviewsHistoryOn(id: Id[Paper]): List[Review] =
    db.history.reviews filter (_.paperId is id) list
  def reviewOf(personId: Id[Person], paperId: Id[Paper]): Option[Review] =
    (reviews filter { r => (r.personId is personId) && (r.paperId is paperId) }).firstOption
  def reviewsOf(paperId: Id[Paper]): List[Review] =
    reviews filter (_.paperId is paperId) list
  def allStaff: List[Person] =
    personRoles filter (r => (r.value is PC_Member) || (r.value is Chair)) flatMap { r =>
      persons filter (_.id is r.personId)
    } list
  def assignmentsOn(id: Id[Paper]): List[Assignment] =
    assignments filter (_.paperId is id) filter (_.value is true) list
  def assignedTo(id: Id[Person]): List[Paper] =
    assignments filter (_.personId is id) filter (_.value is true) flatMap { a =>
      papers filter (_.id is a.paperId)
    } list
  def bidsOf(id: Id[Person]): List[Bid] =
    bids filter (_.personId is id) list
  def bidsOf(personId: Id[Person], paperId: Id[Paper]): Option[Bid] =
    bids filter { b => (b.personId is personId) && (b.paperId is paperId) } firstOption
  def bidsOn(id: Id[Paper]): List[Bid] =
    bids filter (_.paperId is id) list
  def hasRole(id: Id[Person]): Boolean =
    personRoles.filter(_.personId is id).firstOption.isEmpty 
  def nonConflictingPapers(id: Id[Person]): List[Paper] = {
    val userConflicts = bids filter (b => (b.personId is id) && (b.value is Conflict)) map (_.paperId)
    papers filterNot (_.id in userConflicts) list
  }
  def acceptedSubmissions: List[(Paper, List[Person])] = {
    val acceptedPapers = paperDecisions filter (_.value is Accepted) flatMap { d =>
      papers filter (d.paperId is _.id)
    }
    acceptedPapers.list map { p =>
      (p, authorsOf(p.id))
    }
  }
  
  def personWithEmail(email: String): Person = persons filter (_.email is email) first
  def paperWithFile(id: Id[File]): Paper = papers filter (_.fileId is id) first
  def fileWithId(id: Id[File]): File = files filter (_.id is id) first
  def paperWithId(id: Id[Paper]): Paper = papers filter (_.id is id) first
  
  def allPaperDecisions: List[PaperDecision] = paperDecisions list
  def allPersonRoles: List[PersonRole] = personRoles list
  def allPaperIndices: List[PaperIndex] = paperIndices list
  def allAssignments: List[Assignment] = assignments list
  def allPapers: List[Paper] = papers filter (!_.withdrawn) list
  def allPersons: List[Person] = persons list
  def allPapersAndWithdrawn: List[Paper] = papers list
  def allReviews: List[Review] = reviews list
  def allFiles: List[File] = files.map { f =>
    (f.name, f.size, Array[Byte](), (f.id, f.updatedAt, f.updatedBy))
  }.list map File.tupled
  
  def configuration: Configuration = {
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering fromLessThan (_ isBefore _)
    configurations.list maxBy (_.updatedAt)
  }

  def allConfigurations: List[Configuration] = configurations.list

  def fullyDecided: Boolean = paperDecisions.filter { d =>
    (d.value is Temporary_rejected) || (d.value is Undecided) || (d.value is Temporary_accepted)
  }.list.isEmpty
  def reviewerEmails: List[String] =
    personRoles filter (pr => (pr.value is PC_Member) || (pr.value is Chair)) flatMap { p =>
      persons filter (p.personId is _.id)
    } map (_.email) list
  def chairEmails: List[String] =
    personRoles filter (pr => (pr.value is Chair)) flatMap { p =>
      persons filter (p.personId is _.id)
    } map (_.email) list
    
  def acceptedEmails: List[String] = statusEmails(Accepted)
  def rejectedEmails: List[String] = statusEmails(Rejected)
  private def statusEmails(status: Decision): List[String] = {
    val withStatusPaper = paperDecisions filter (_.value is Accepted) flatMap { d =>
      papers filter (d.paperId is _.id)
    }
    withStatusPaper.list flatMap { p =>
      authorsOf(p.id) map (_.email)
    } distinct
  }
}
