package models

import play.api.db.slick.Config.driver.simple._
import com.github.tototoshi.slick.H2JodaSupport._
import org.joda.time.DateTime
import scala.slick.lifted.Query

trait RepoQuery[T <: Table[M] with RepoTable[M], M <: Model[M]] extends ImplicitMappers { 
  this: TableQuery[T] =>
  
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)
  
  def latests(implicit s: Session) = {
    val maxDates = this.groupBy (_.id) map { case (id, xs) => (id, xs.map(_.updatedAt).max) }

    /** Not sure if this would be better than a join... */
    // this filter { r =>
    //   maxDates.filter { m =>
    //     (r.id is m._1) && (r.updatedAt is m._2)
    //   }.exists
    // }

    for {
      c <- this
      s <- maxDates if ((c.id is s._1) && (c.updatedAt is s._2))
    } yield c
  }
  
  def withId(id: Id[M])(implicit s: Session): M = latests.filter(_.id is id).first
  def all(implicit s: Session): List[M] = latests.list
  def count(implicit s: Session): Int = all.size
  def ins(m: M)(implicit s: Session): Id[M] = { this insert m; m.id }
  def insAll(l: List[M])(implicit s: Session): List[Id[M]] = l map ins
}

object Topics extends TableQuery(new TopicTable(_)) with RepoQuery[TopicTable, Topic] {
  def of(id: Id[Paper])(implicit s: Session): List[Topic] = {
    val allPaperTopics = PaperTopics.filter(_.paperid is id)
    val lastShotPaperTopics = allPaperTopics filter (_.updatedAt is allPaperTopics.map(_.updatedAt).max)
    lastShotPaperTopics.flatMap{ p => Topics.latests.filter(_.id is p.topicid) }.list
  }
}

object Persons extends TableQuery(new PersonTable(_)) with RepoQuery[PersonTable, Person] {
  override def ins(p: Person)(implicit s: Session): Id[Person] = {
    Persons.filter(_.email is p.email).firstOption match {
      case None => super.ins(p)
      case Some(old) => {
        val newPerson = p.copy(metadata=(old.id, p.updatedAt, p.updatedBy), role=old.role)
        if(p != newPerson)
          super.ins(newPerson)
        old.id
      }
    }
  }
  def withEmail(email: String)(implicit s: Session): Person = Persons.latests.filter(_.email is email).first
}

object Papers extends TableQuery(new PaperTable(_)) with RepoQuery[PaperTable, Paper] {
  def of(email: String)(implicit s: Session): List[Id[Paper]] = {
    val personId = Persons.withEmail(email).id
    Authors.latests.filter(_.personid is personId).groupBy(_.paperid).map(_._1).list
  }
  def withFile(id: Id[File])(implicit s: Session): Id[Paper] = {
    this.filter(_.fileid is id).first.id
  }
}

object PaperTopics extends TableQuery(new PaperTopicTable(_)) with RepoQuery[PaperTopicTable, PaperTopic] {
}

object Authors extends TableQuery(new AuthorTable(_)) with RepoQuery[AuthorTable, Author] {
  def of(id: Id[Paper])(implicit s: Session): List[Person] = {
    val allAuthors = Authors.latests.filter(_.paperid is id)
    val lastShotAuthors = allAuthors filter (_.updatedAt is allAuthors.map(_.updatedAt).max)
    lastShotAuthors.flatMap{ p => Persons.latests.filter(_.id is p.personid) }.list
  }
}

object Comments extends TableQuery(new CommentTable(_)) with RepoQuery[CommentTable, Comment] {
}
object Reviews extends TableQuery(new ReviewTable(_)) with RepoQuery[ReviewTable, Review] {
}
object Files extends TableQuery(new FileTable(_)) with RepoQuery[FileTable, File] {
  /** Files.all does not returns file blobs. An alternative to this hack 
    * would be to have separated tables for metadata and content. */
  override def all(implicit s: Session): List[File] = this.map {
    f => ((f.id, f.updatedAt, f.updatedBy), f.name, f.size, Array[Byte]())
  }.list map File.tupled
}
object Emails extends TableQuery(new EmailTable(_)) with RepoQuery[EmailTable, Email] {
}
object Bids extends TableQuery(new BidTable(_)) with RepoQuery[BidTable, Bid] {
  def of(id: Id[Person])(implicit s: Session): List[Bid] = latests.filter(_.personid is id).list
  def of(personId: Id[Person], paperId: Id[Paper])(implicit s: Session): Option[Bid] =
    latests.filter(b => (b.personid is personId) && (b.paperid is paperId)).firstOption
  override def insAll(l: List[Bid])(implicit s: Session): List[Id[Bid]] = {
    val previous: List[Bid] = this.latests.list
    l map { bid =>
      previous.find(b => b.paperid == bid.paperid && b.personid == bid.personid) match {
        case Some(old) if old.value != bid.value =>
          this ins bid.copy(metadata=(old.id, bid.updatedAt, bid.updatedBy))
        case None if bid.value != models.BidValue.Maybe =>
          this ins bid
        case _ => bid.id
      }
    }
  }
}
object Assignments extends TableQuery(new AssignmentTable(_)) with RepoQuery[AssignmentTable, Assignment] {
}
