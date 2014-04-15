package models

import play.api.db.slick.Config.driver.simple._
import com.github.tototoshi.slick.H2JodaSupport._
import org.joda.time.DateTime
import scala.slick.lifted.Query

trait RepoQuery[T <: Table[M] with RepoTable[M], M <: Model[M]] extends ImplicitMappers { 
  this: TableQuery[T] =>
  
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)
  
  def latests(implicit s: Session) = {
    // With scala.collection.immutable.List methods
    // this.list.groupBy(_.id).flatMap {
    //   case (_, xs) => xs sortBy (_.updatedAt) take 1  
    // }.toList

    // In Slick:
    this.groupBy (_.id) flatMap {
      case (_, xs) => xs sortBy (_.updatedAt) take 1  
    }
  }
  
  def withId(id: Id[M])(implicit s: Session): M = latests.filter(_.id is id).first
  def all(implicit s: Session): List[M] = latests.list
  def count(implicit s: Session): Int = all.size
  def ins(m: M)(implicit s: Session): Id[M] = { this insert m; m.id }
  def updt(m: M)(implicit s: Session): Id[M] = { this forceInsert m; m.id }
  def insAll(l: List[M])(implicit s: Session): List[Id[M]] = l map ins
  def updtAll(l: List[M])(implicit s: Session): List[Id[M]] = l map updt
  def lastShot[T <: Model[T]](l: List[T]): List[T] = l.filter(_.updatedAt == l.minBy(_.updatedAt).updatedAt)
}

object Topics extends TableQuery(new TopicTable(_)) with RepoQuery[TopicTable, Topic] {
  def of(paper: Paper)(implicit s: Session): List[Topic] = {
    lastShot(PaperTopics.latests.filter(_.paperid is paper.id).list) flatMap { p =>
      Topics.latests.filter(_.id is p.topicid).list
    }
  }
}

object Persons extends TableQuery(new PersonTable(_)) with RepoQuery[PersonTable, Person] {
  def saveAll(l: List[Person])(implicit s: Session): List[Id[Person]] = {
    l.map { p: Person =>
      Persons.filter(_.email is p.email).firstOption match {
         // TODO restrict this according to updatedBy values
        case Some(old) => Persons.ins(p.copy(metadata=(old.id, p.updatedAt, p.updatedBy)))
        case None => Persons.ins(p)
      }
    }
  }
}

object Papers extends TableQuery(new PaperTable(_)) with RepoQuery[PaperTable, Paper] {
}

object PaperTopics extends TableQuery(new PaperTopicTable(_)) with RepoQuery[PaperTopicTable, PaperTopic] {
}

object Authors extends TableQuery(new AuthorTable(_)) with RepoQuery[AuthorTable, Author] {
  def of(paper: Paper)(implicit s: Session): List[Person] =
    lastShot(Authors.latests.filter(_.paperid is paper.id).list) flatMap { p =>
      Persons.latests.filter(_.id is p.personid).list
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
