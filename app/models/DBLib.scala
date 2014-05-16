package models

import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import PersonRole._
import PaperType._
import ReviewConfidence._
import ReviewEvaluation._
import BidValue._
import java.sql.Timestamp

case class Id[M](value: IdType)

trait Model[M] {
  this: M with Product =>
  val metadata: MetaData[M]
  lazy val (id, updatedAt, updatedBy) = metadata

  def withId(newId: Id[M]): M = ((this: M) match {
    case x: Topic => x.copy(metadata=(Id[Topic](newId.value), x.updatedAt, x.updatedBy))
    case x: Person => x.copy(metadata=(Id[Person](newId.value), x.updatedAt, x.updatedBy))
    case x: Paper => x.copy(metadata=(Id[Paper](newId.value), x.updatedAt, x.updatedBy))
    case x: File => x.copy(metadata=(Id[File](newId.value), x.updatedAt, x.updatedBy))
    case x: Comment => x.copy(metadata=(Id[Comment](newId.value), x.updatedAt, x.updatedBy))
  }).asInstanceOf[M]
}

object Connection {
  def database(): Database = Database(new DateTime())
  def insert(ms: Model[_]*)(implicit s: Session): (Database, Database) = insertAll(ms)
  def insertAll(ms: Seq[_])(implicit s: Session): (Database, Database) = {
    val now: DateTime = new DateTime()
    ms foreach { _ match {
      case m: Topic => TableQuery[TopicTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Person => TableQuery[PersonTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Paper => TableQuery[PaperTable] insert m.copy(metadata=(m.id, now, ""))
      case m: PaperTopic => TableQuery[PaperTopicTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Author => TableQuery[AuthorTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Comment => TableQuery[CommentTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Review => TableQuery[ReviewTable] insert m.copy(metadata=(m.id, now, ""))
      case m: File => TableQuery[FileTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Email => TableQuery[EmailTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Bid => TableQuery[BidTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Assignment => TableQuery[AssignmentTable] insert m.copy(metadata=(m.id, now, ""))
    }}
    (Database(now minusMillis 1), Database(now))
  }
}

case class Database(val time: DateTime, val history: Boolean = false) extends ImplicitMappers {
  def asOf(time: DateTime): Database = this copy (time=time)
  def equals(database: Database): Boolean = this.basis == database.basis
  def basis(): DateTime = ???
    
  private def timeMod[T <: Table[M] with RepoTable[M], M <: Model[M]](table: TableQuery[T]) = {
    // : Query[T, M] = {
    // TODO: Use this.time
    if(history) {
      val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)
      val maxDates = table.groupBy (_.id) map { case (id, xs) => (id, xs.map(_.updatedAt).max) }
      for {
        c <- table
        s <- maxDates if ((c.id is s._1) && (c.updatedAt is s._2))
      } yield c
    } else {
      table
    }
  }
  
  val topics = timeMod[TopicTable, Topic](TableQuery[TopicTable])
  val persons = timeMod[PersonTable, Person](TableQuery[PersonTable])
  val papers = timeMod[PaperTable, Paper](TableQuery[PaperTable])
  val paperTopics = timeMod[PaperTopicTable, PaperTopic](TableQuery[PaperTopicTable])
  val authors = timeMod[AuthorTable, Author](TableQuery[AuthorTable])
  val comments = timeMod[CommentTable, Comment](TableQuery[CommentTable])
  val reviews = timeMod[ReviewTable, Review](TableQuery[ReviewTable])
  val files = timeMod[FileTable, File](TableQuery[FileTable])
  val emails = timeMod[EmailTable, Email](TableQuery[EmailTable])
  val bids = timeMod[BidTable, Bid](TableQuery[BidTable])
  val assignments = timeMod[AssignmentTable, Assignment](TableQuery[AssignmentTable])
}


// Tables
trait ImplicitMappers {
  implicit def idMapper[T <: Model[T]] = MappedColumnType.base[Id[T], IdType](_.value, Id[T])
  implicit def dateTimeMapper = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.getMillis), ts => new DateTime(ts.getTime))
}

trait EnumMapper {
  this: Enumeration =>
  implicit val slickMapping = MappedColumnType.base[Value, Int](_.id, this.apply)
}

trait RepoTable[M <: Model[M]] extends ImplicitMappers {
  this: Table[M] =>
  def id = column[Id[M]]("ID", O.AutoInc)
  def updatedAt = column[DateTime]("UPDATEDAT")
  def updatedBy = column[String]("UPDATEDBY", O.DBType("TEXT"))
}

class TopicTable(tag: Tag) extends Table[Topic](tag, "TOPIC") with RepoTable[Topic] {
  def name = column[String]("NAME", O.DBType("TEXT"))
  def * = (name, (id, updatedAt, updatedBy)) <> (Topic.tupled, Topic.unapply)
}

class PersonTable(tag: Tag) extends Table[Person](tag, "PERSON") with RepoTable[Person] {
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  def organization = column[String]("ORGANIZATION")
  def role = column[PersonRole]("ROLE")
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  def * = (firstname, lastname, organization, role, email, (id, updatedAt, updatedBy)) <> (Person.tupled, Person.unapply)
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

class PaperTopicTable(tag: Tag) extends Table[PaperTopic](tag, "PAPERTOPIC") with RepoTable[PaperTopic] {
  def paperid = column[Id[Paper]]("PAPERID")
  def topicid = column[Id[Topic]]("TOPICID")
  def * = (paperid, topicid, (id, updatedAt, updatedBy)) <> (PaperTopic.tupled, PaperTopic.unapply)
}

class AuthorTable(tag: Tag) extends Table[Author](tag, "AUTHOR") with RepoTable[Author] {
  def paperid = column[Id[Paper]]("PAPERID")
  def personid = column[Id[Person]]("PERSONID")
  def position = column[Int]("POSITION")
  def * = (paperid, personid, position, (id, updatedAt, updatedBy)) <> (Author.tupled, Author.unapply)
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
  def * = (paperid, personid, (id, updatedAt, updatedBy)) <> (Assignment.tupled, Assignment.unapply)
}
