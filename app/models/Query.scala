package models

import Mappers.idSlickMapper
import PersonRole._
import play.api.db.slick.Config.driver.simple._
import scala.language.postfixOps

case class Query(db: Database) {
  implicit val session: Session = db.session
  import db._
  
  def topicsOf(id: Id[Paper]): List[Topic] =
    paperTopics filter (_.paperid is id) flatMap { pt => topics filter (_.id is pt.topicid) } list
  def roleOf(id: Id[Person]): PersonRole =
    roles.filter(_.personid is id).firstOption map (_.value) getOrElse Author
  def papersOf(id: Id[Person]): List[Paper] =
    authors filter (_.personid is id) flatMap { a => papers.filter(_.id is a.paperid) } list
  def indexOf(id: Id[Paper]): Int =
    (paperIndices sortBy (_.updatedAt) map(_.paperid) list).indexOf(id) + 1
  def authorsOf(id: Id[Paper]): List[Person] =
    authors filter (_.paperid is id) flatMap { a => persons filter (_.id is a.personid) } list
  def commentsOf(id: Id[Paper]): List[Comment] =
    comments filter (_.paperid is id) list
  def reviewsOf(id: Id[Paper]): List[Review] =
    reviews filter (_.paperid is id) list
  def notReviewed(personId: Id[Person], paperId: Id[Paper]): Boolean =
    (reviews filter { r => (r.personid is personId) && (r.paperid is paperId) }).firstOption.isEmpty
  def allStaff: List[Person] =
    roles filter (r => (r.value is Reviewer) || (r.value is Chair)) flatMap { r =>
      persons filter (_.id is r.personid)
    } list
  def assignmentsOn(id: Id[Paper]): List[Assignment] =
    assignments filter (_.paperid is id) filter (_.value is true) list
  def assignedTo(id: Id[Person]): List[Paper] =
    assignments filter (_.personid is id) filter (_.value is true) flatMap { a =>
      papers filter (_.id is a.paperid)
    } list
  def bidsOf(id: Id[Person]): List[Bid] =
    bids filter (_.personid is id) list
  def bidsOf(personId: Id[Person], paperId: Id[Paper]): Option[Bid] =
    bids filter { b => (b.personid is personId) && (b.paperid is paperId) } firstOption
  def bidsOn(id: Id[Paper]): List[Bid] =
    bids filter (_.paperid is id) list
  
  def personWithEmail(email: String): Person = persons filter (_.email is email) first
  def paperWithFile(id: Id[File]): Paper = papers filter (_.fileid is id) first
  def fileWithId(id: Id[File]): File = files filter (_.id is id) first
  def paperWithId(id: Id[Paper]): Paper = papers filter (_.id is id) first
  
  def allPaperIndices: List[PaperIndex] = paperIndices list
  def allAssignments: List[Assignment] = assignments list
  def allPapers: List[Paper] = papers list
  def allTopics: List[Topic] = topics list
  def allFiles: List[File] = files.map { f =>
    (f.name, f.size, Array[Byte](), (f.id, f.updatedAt, f.updatedBy))
  }.list map File.tupled
}
