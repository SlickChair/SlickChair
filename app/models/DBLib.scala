package models

import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import PersonRole._
import PaperType._
import ReviewConfidence._
import ReviewEvaluation._
import BidValue._
import java.sql.Timestamp
import java.util.UUID
import java.nio.ByteBuffer

case class Id[M](value: IdType)

trait Model[M] {
  this: Product with M { def metadata: Metadata[M] } =>
  val (id, updatedAt, updatedBy) = metadata
  
  protected def pk(id1: Id[_], id2: Id[_]): Id[M] = Id[M](new UUID(
    id1.value.getMostSignificantBits() ^ id2.value.getMostSignificantBits(),
    id1.value.getLeastSignificantBits() ^ id2.value.getLeastSignificantBits() ^ getClass().hashCode
  ))

  protected def pk(id: Id[_]): Id[M] = Id[M](new UUID(
    id.value.getMostSignificantBits(), id.value.getLeastSignificantBits() ^ getClass().hashCode
  ))
  
  protected def pk(s: String): Id[M] = {
    val l = s.padTo(16, 'a').toCharArray map (_.toByte) grouped 8 map (ByteBuffer.wrap(_).getLong) take 2
    Id[M](new UUID(l.next(), l.next() ^ s.hashCode))    
  }
  
  def withId(newId: Id[M]): M = ((this: M) match {
    case x: Topic => x.copy(metadata=(Id[Topic](newId.value), x.updatedAt, x.updatedBy))
    case x: Person => x.copy(metadata=(Id[Person](newId.value), x.updatedAt, x.updatedBy))
    case x: Paper => x.copy(metadata=(Id[Paper](newId.value), x.updatedAt, x.updatedBy))
    case x: File => x.copy(metadata=(Id[File](newId.value), x.updatedAt, x.updatedBy))
    case x: Comment => x.copy(metadata=(Id[Comment](newId.value), x.updatedAt, x.updatedBy))
  }).asInstanceOf[M]
}

case class Connection(session: Session) {
  def database(): Database = Database(new DateTime(), session)
  def insert(ms: List[_]): (Database, Database) = {
    implicit val s: Session = session
    val now: DateTime = new DateTime()
    ms foreach { _ match {
      case m: Topic => TableQuery[TopicTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Person => TableQuery[PersonTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Paper => TableQuery[PaperTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Role => TableQuery[RoleTable] insert m.copy(metadata=(m.id, now, ""))
      case m: PaperIndex => TableQuery[PaperIndexTable] insert m.copy(metadata=(m.id, now, ""))
      case m: PaperTopic => TableQuery[PaperTopicTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Author => TableQuery[AuthorTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Comment => TableQuery[CommentTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Review => TableQuery[ReviewTable] insert m.copy(metadata=(m.id, now, ""))
      case m: File => TableQuery[FileTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Email => TableQuery[EmailTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Bid => TableQuery[BidTable] insert m.copy(metadata=(m.id, now, ""))
      case m: Assignment => TableQuery[AssignmentTable] insert m.copy(metadata=(m.id, now, ""))
    }}
    (Database(now minusMillis 1, session), Database(now, session))
  }
}

case class Database(val time: DateTime, val session: Session, val history: Boolean = false) extends ImplicitMappers {
  def asOf(time: DateTime): Database = this copy (time=time)
  def equals(database: Database): Boolean = this.basis == database.basis
  def basis(): DateTime = ???
  
  private def timeMod[T <: Table[M] with RepoTable[M], M <: Model[M]](table: TableQuery[T]) = {
    // : Query[T, M] = {
    // TODO: Use this.time
    if(history) {
      table
    } else {
      val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)
      val maxDates = table.groupBy (_.id) map { case (id, xs) => (id, xs.map(_.updatedAt).max) }
      for {
        c <- table
        s <- maxDates if ((c.id is s._1) && (c.updatedAt is s._2))
      } yield c
    }
  }
  
  val topics = timeMod[TopicTable, Topic](TableQuery[TopicTable])
  val persons = timeMod[PersonTable, Person](TableQuery[PersonTable])
  val roles = timeMod[RoleTable, Role](TableQuery[RoleTable])
  val papers = timeMod[PaperTable, Paper](TableQuery[PaperTable])
  val paperIndices = timeMod[PaperIndexTable, PaperIndex](TableQuery[PaperIndexTable])
  val paperTopics = timeMod[PaperTopicTable, PaperTopic](TableQuery[PaperTopicTable])
  val authors = timeMod[AuthorTable, Author](TableQuery[AuthorTable])
  val comments = timeMod[CommentTable, Comment](TableQuery[CommentTable])
  val reviews = timeMod[ReviewTable, Review](TableQuery[ReviewTable])
  val files = timeMod[FileTable, File](TableQuery[FileTable])
  val emails = timeMod[EmailTable, Email](TableQuery[EmailTable])
  val bids = timeMod[BidTable, Bid](TableQuery[BidTable])
  val assignments = timeMod[AssignmentTable, Assignment](TableQuery[AssignmentTable])
}

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
  def id = column[Id[M]]("ID")
  // def id = column[Id[M]]("ID", O.AutoInc)
  def updatedAt = column[DateTime]("UPDATEDAT")
  def updatedBy = column[String]("UPDATEDBY", O.DBType("TEXT"))
}
