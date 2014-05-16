package models

import play.api.db.slick.Config.driver.simple._
import org.joda.time.DateTime

case class Query(db: Database)(implicit s: Session) extends ImplicitMappers {
  def topicsOf(id: Id[Paper]): List[Topic] = {
    val allPaperTopics = db.paperTopics filter (_.paperid is id)
    val lastShotPaperTopics = allPaperTopics filter (_.updatedAt is allPaperTopics.map(_.updatedAt).max)
    lastShotPaperTopics.flatMap{ p => db.topics filter (_.id is p.topicid) }.list
  }
  def personWithEmail(email: String): Person =
    db.persons.filter(_.email is email).first
  def papersOf(email: String): List[Id[Paper]] =
    db.authors.filter(_.personid is personWithEmail(email).id).groupBy(_.paperid).map(_._1).list
  def paperWithFile(id: Id[File]): Id[Paper] =
    db.papers.filter(_.fileid is id).first.id
  def authorsOf(id: Id[Paper]): List[Person] = {
    val allAuthors = db.authors filter (_.paperid is id)
    val lastShotAuthors = allAuthors filter (_.updatedAt is allAuthors.map(_.updatedAt).max)
    lastShotAuthors.flatMap{ p => db.persons.filter(_.id is p.personid) }.list
  }
  def bidsOf(id: Id[Person]): List[Bid] =
    db.bids.filter(_.personid is id).list
  def bidsOf(personId: Id[Person], paperId: Id[Paper]): Option[Bid] =
    db.bids.filter(b => (b.personid is personId) && (b.paperid is paperId)).firstOption
  def fileWithId(id: Id[File]): File =
    db.files.filter(_.id is id).first
  def paperWithId(id: Id[Paper]): Paper =
    db.papers.filter(_.id is id).first
  def allPapers: List[Paper] = db.papers.list
  def allTopics: List[Topic] = db.topics.list
  def allFiles: List[File] = db.files.map { f =>
    (f.name, f.size, Array[Byte](), (f.id, f.updatedAt, f.updatedBy))
  }.list map File.tupled
}
