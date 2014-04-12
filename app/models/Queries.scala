package models

import play.api.db.slick.Config.driver.simple._
import com.github.tototoshi.slick.H2JodaSupport._
import scala.slick.lifted.Query

trait RepoQuery[T <: Table[M] with RepoTable[M], M <: Model[M]] extends ImplicitMappers { 
  this: TableQuery[T] =>
  // TODO: historically filter these
  def latests(implicit s: Session): Query[T,T#TableElementType] = this.filter(_ => true)
  def withId(id: Id[M])(implicit s: Session): M = latests.filter(_.id is id).first
  def all(implicit s: Session): List[M] = latests.list
  def count(implicit s: Session): Int = all.size
  def ins(m: M)(implicit s: Session): Id[M] = (this returning this.map(_.id)) insert m
  def updt(m: M)(implicit s: Session): Id[M] = (this returning this.map(_.id)) forceInsert m
  def insAll(l: List[M])(implicit s: Session): List[Id[M]] = l map ins
  def updtAll(l: List[M])(implicit s: Session): List[Id[M]] = l map updt
  def saveAll(l: List[M])(implicit s: Session): List[Id[M]] = insAll(l) // TODO
}

object Topics extends TableQuery(new TopicTable(_)) with RepoQuery[TopicTable, Topic] {
  def of(paper: Paper)(implicit s: Session): List[Topic] =
    (for {
      p <- PaperTopics.latests if p.paperid is paper.id
      t <- Topics.latests if t.id is p.topicid
    } yield t).list
}

object Persons extends TableQuery(new PersonTable(_)) with RepoQuery[PersonTable, Person] {
  def withEmail(email: String)(implicit s: Session): Option[Person] = { // TODO -> List[Paper]
    None // TODO: DO Authors.withEmail(email)
  }
}

object Papers extends TableQuery(new PaperTable(_)) with RepoQuery[PaperTable, Paper] {
  def withEmail(email: String)(implicit s: Session): Option[Paper] = { // TODO -> List[Paper]
    None // TODO: DO Authors.withEmail(email)
  }
}

object PaperTopics extends TableQuery(new PaperTopicTable(_)) with RepoQuery[PaperTopicTable, PaperTopic] {
}

object Authors extends TableQuery(new AuthorTable(_)) with RepoQuery[AuthorTable, Author] {
  def of(paper: Paper)(implicit s: Session): List[Person] = 
    (for {
      a <- Authors.latests if a.paperid is paper.id
      p <- Persons.latests if p.id is a.personid
    } yield p).list
}

object Comments extends TableQuery(new CommentTable(_)) with RepoQuery[CommentTable, Comment] {
}

object Reviews extends TableQuery(new ReviewTable(_)) with RepoQuery[ReviewTable, Review] {
}

object Files extends TableQuery(new FileTable(_)) with RepoQuery[FileTable, File] {
}

object Emails extends TableQuery(new EmailTable(_)) with RepoQuery[EmailTable, Email] {
}
