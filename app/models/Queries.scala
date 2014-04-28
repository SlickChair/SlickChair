package models

import play.api.db.slick.Config.driver.simple._
import com.github.tototoshi.slick.H2JodaSupport._
import org.joda.time.DateTime
import scala.slick.lifted.Query

trait RepoQuery[T <: Table[M] with RepoTable[M], M <: Model[M]] extends ImplicitMappers { 
  this: TableQuery[T] =>
  
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)
  
  def latests(implicit s: Session) = {
    val maxDates = this.groupBy (_.id) map { case (_, xs) => xs.map(_.updatedAt).max }
    this filter (_.updatedAt in maxDates)
  }
  
  def withId(id: Id[M])(implicit s: Session): M = latests.filter(_.id is id).first
  def all(implicit s: Session): List[M] = latests.list
  def count(implicit s: Session): Int = all.size
  def ins(m: M)(implicit s: Session): Id[M] = { this insert m; m.id }
  def insAll(l: List[M])(implicit s: Session): List[Id[M]] = l map ins
}

object Topics extends TableQuery(new TopicTable(_)) with RepoQuery[TopicTable, Topic] {
  def of(paper: Paper)(implicit s: Session): List[Topic] = {
    val allPaperTopics = PaperTopics.filter(_.paperid is paper.id)
    val lastShotPaperTopics = allPaperTopics filter (_.updatedAt is allPaperTopics.map(_.updatedAt).max)
    lastShotPaperTopics.flatMap{ p => Topics.latests.filter(_.id is p.topicid) }.list
  }
}

object Persons extends TableQuery(new PersonTable(_)) with RepoQuery[PersonTable, Person] {
  def save(p: Person)(implicit s: Session): Id[Person] = {
    Persons.filter(_.email is p.email).firstOption match {
      case None => Persons.ins(p)
      case Some(old) => {
        val newPerson = p.copy(metadata=(old.id, p.updatedAt, p.updatedBy), role=old.role)
        if(p != newPerson)
          Persons.ins(newPerson)
        p.id
      }
    }
  }
  def saveAll(l: List[Person])(implicit s: Session): List[Id[Person]] = l map save
  def withEmail(email: String)(implicit s: Session): Person = Persons.latests.filter(_.email is email).first
}

object Papers extends TableQuery(new PaperTable(_)) with RepoQuery[PaperTable, Paper] {
  def of(email: String)(implicit s: Session): List[Id[Paper]] = {
    val personId = Persons.withEmail(email).id
    Authors.latests.filter(_.personid is personId).groupBy(_.paperid).map(_._1).list
  }
}

object PaperTopics extends TableQuery(new PaperTopicTable(_)) with RepoQuery[PaperTopicTable, PaperTopic] {
}

object Authors extends TableQuery(new AuthorTable(_)) with RepoQuery[AuthorTable, Author] {
  def of(paper: Paper)(implicit s: Session): List[Person] = {
    val allAuthors = Authors.filter(_.paperid is paper.id)
    val lastShotAuthors = allAuthors filter (_.updatedAt is allAuthors.map(_.updatedAt).max)
    lastShotAuthors.flatMap{ p => Persons.latests.filter(_.id is p.personid) }.list
  }
}

object Comments extends TableQuery(new CommentTable(_)) with RepoQuery[CommentTable, Comment] {
}
object Reviews extends TableQuery(new ReviewTable(_)) with RepoQuery[ReviewTable, Review] {
}
object Files extends TableQuery(new FileTable(_)) with RepoQuery[FileTable, File] {
}
object Emails extends TableQuery(new EmailTable(_)) with RepoQuery[EmailTable, Email] {
}
object Bids extends TableQuery(new BidTable(_)) with RepoQuery[BidTable, Bid] {
}
object Assignments extends TableQuery(new AssignmentTable(_)) with RepoQuery[AssignmentTable, Assignment] {
}
