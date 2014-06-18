package models

import java.nio.ByteBuffer
import java.util.UUID
import Array.canBuildFrom
import org.joda.time.DateTime
import Mappers.{dateTimeSlickMapper, idSlickMapper}
import scala.slick.lifted.TableQuery
import play.api.db.slick.Config.driver.simple._

case class Id[M](value: IdType)

trait Model[M] {
  this: Product with M { def metadata: Metadata[M] } =>
  val (id, updatedAt, updatedBy) = metadata
  
  protected def pk(id1: Id[_], id2: Id[_]): Id[M] = Id[M](new UUID(
    id1.value.getMostSignificantBits() ^ id2.value.getMostSignificantBits(),
    id1.value.getLeastSignificantBits() ^ id2.value.getLeastSignificantBits() ^ getClass().toString.hashCode
  ))

  protected def pk(id: Id[_]): Id[M] = Id[M](new UUID(
    id.value.getMostSignificantBits(), id.value.getLeastSignificantBits() ^ getClass().toString.hashCode
  ))
  
  protected def pk(s: String): Id[M] = {
    val l = s.padTo(16, 'a').toCharArray map (_.toByte) grouped 8 map (ByteBuffer.wrap(_).getLong) take 2
    Id[M](new UUID(l.next() ^ getClass().toString.hashCode, l.next() ^ s.hashCode))    
  }
}

case class Connection(session: Session) {
  implicit val s: Session = session
  
  // Could this be made generic with a macro? See http://goo.gl/8sxnrg
  private def setTime[T](model: Model[T], time: DateTime): Model[T] = {
    model match {
      case m: Person => m.copy(metadata=(m.id, time, ""))
      case m: Paper => m.copy(metadata=(m.id, time, ""))
      case m: PersonRole => m.copy(metadata=(m.id, time, ""))
      case m: PaperIndex => m.copy(metadata=(m.id, time, ""))
      case m: PaperAuthor => m.copy(metadata=(m.id, time, ""))
      case m: PaperDecision => m.copy(metadata=(m.id, time, ""))
      case m: Comment => m.copy(metadata=(m.id, time, ""))
      case m: Review => m.copy(metadata=(m.id, time, ""))
      case m: File => m.copy(metadata=(m.id, time, ""))
      case m: Email => m.copy(metadata=(m.id, time, ""))
      case m: Bid => m.copy(metadata=(m.id, time, ""))
      case m: Assignment => m.copy(metadata=(m.id, time, ""))
      case m: Configuration => m.copy(metadata=(m.id, time, ""))
    } 
  }

  def database(): Database = Database(new DateTime(), session)
  
  def insert[M <: Model[M]](m: M): (Database, Database) = insert(List(m))
  def insert[M <: Model[M]](xs: List[M]): (Database, Database) = {
    val now: DateTime = new DateTime()
    xs.headOption.map { _ match {
      case _: Person =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.persons.list.map(setTime(_, now))
        TableQuery[PersonTable] insertAll (allNew.toList.asInstanceOf[List[Person]]: _*)
      case _: Paper =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.papers.list.map(setTime(_, now))
        TableQuery[PaperTable] insertAll (allNew.toList.asInstanceOf[List[Paper]]: _*)
      case _: PersonRole =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.personRoles.list.map(setTime(_, now))
        TableQuery[PersonRoleTable] insertAll (allNew.toList.asInstanceOf[List[PersonRole]]: _*)
      case _: PaperIndex =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.paperIndices.list.map(setTime(_, now))
        TableQuery[PaperIndexTable] insertAll (allNew.toList.asInstanceOf[List[PaperIndex]]: _*)
      case _: PaperAuthor =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.paperAuthors.list.map(setTime(_, now))
        TableQuery[PaperAuthorTable] insertAll (allNew.toList.asInstanceOf[List[PaperAuthor]]: _*)
      case _: PaperDecision =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.paperDecisions.list.map(setTime(_, now))
        TableQuery[PaperDecisionTable] insertAll (allNew.toList.asInstanceOf[List[PaperDecision]]: _*)
      case _: Comment =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.comments.list.map(setTime(_, now))
        TableQuery[CommentTable] insertAll (allNew.toList.asInstanceOf[List[Comment]]: _*)
      case _: Review =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.reviews.list.map(setTime(_, now))
        TableQuery[ReviewTable] insertAll (allNew.toList.asInstanceOf[List[Review]]: _*)
      case _: File =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.files.list.map(setTime(_, now))
        TableQuery[FileTable] insertAll (allNew.toList.asInstanceOf[List[File]]: _*)
      case _: Email =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.emails.list.map(setTime(_, now))
        TableQuery[EmailTable] insertAll (allNew.toList.asInstanceOf[List[Email]]: _*)
      case _: Bid =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.bids.list.map(setTime(_, now))
        TableQuery[BidTable] insertAll (allNew.toList.asInstanceOf[List[Bid]]: _*)
      case _: Assignment =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.assignments.list.map(setTime(_, now))
        TableQuery[AssignmentTable] insertAll (allNew.toList.asInstanceOf[List[Assignment]]: _*)
      case _: Configuration =>
        val allNew = xs.map(setTime(_, now)).toSet -- database.configurations.list.map(setTime(_, now))
        TableQuery[ConfigurationTable] insertAll (allNew.toList.asInstanceOf[List[Configuration]]: _*)
    }}
    (Database(now minusMillis 1, session), Database(now, session))
  }
}

case class Database(val date: DateTime, val session: Session, val withHistory: Boolean = false) {
  implicit val implicitSession = session
  // def asOf(date: DateTime): Database = this copy (date=date)
  // def equals(database: Database): Boolean = this.basis == database.basis
  // def basis(): DateTime = ???
  // def history: Database = this.copy(withHistory=true)
  
  // private type Table = PersonTable
  private def table[T <: Table[M] with RepoTable[M], M <: Model[M]](table: TableQuery[T]) = {
    // TODO: Use this.date
    if(withHistory) {
      table
    } else {
      val dateTimeOrdering: Ordering[DateTime] = Ordering fromLessThan (_ isAfter _)
      val maxDates = table.groupBy (_.id) map { case (id, xs) => (id, xs.map(_.updatedAt).max) }
      for {
        c <- table
        s <- maxDates if ((c.id is s._1) && (c.updatedAt is s._2))
      } yield c
    }
  }

  val persons = table[PersonTable, Person](TableQuery[PersonTable])
  val personRoles = table[PersonRoleTable, PersonRole](TableQuery[PersonRoleTable])
  val papers = table[PaperTable, Paper](TableQuery[PaperTable])
  val paperIndices = table[PaperIndexTable, PaperIndex](TableQuery[PaperIndexTable])
  val paperAuthors = table[PaperAuthorTable, PaperAuthor](TableQuery[PaperAuthorTable])
  val paperDecisions = table[PaperDecisionTable, PaperDecision](TableQuery[PaperDecisionTable])
  val comments = table[CommentTable, Comment](TableQuery[CommentTable])
  val reviews = table[ReviewTable, Review](TableQuery[ReviewTable])
  val files = table[FileTable, File](TableQuery[FileTable])
  val emails = table[EmailTable, Email](TableQuery[EmailTable])
  val bids = table[BidTable, Bid](TableQuery[BidTable])
  val assignments = table[AssignmentTable, Assignment](TableQuery[AssignmentTable])
  val configurations = table[ConfigurationTable, Configuration](TableQuery[ConfigurationTable])
}

trait RepoTable[M <: Model[M]] {
  this: Table[M] =>
  def id = column[Id[M]]("ID")
  // def id = column[Id[M]]("ID", O.AutoInc)
  def updatedAt = column[DateTime]("UPDATEDAT")
  def updatedBy = column[String]("UPDATEDBY", O.DBType("TEXT"))
}
