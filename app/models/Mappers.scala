package models

import java.sql.Timestamp
import java.util.UUID
import org.joda.time.DateTime
import play.api.data.FormError
import play.api.data.Forms.{mapping, nonEmptyText, of}
import play.api.data.Mapping
import play.api.data.format.Formats.stringFormat
import play.api.data.format.Formatter
import play.api.db.slick.Config.driver.simple._
import play.api.mvc.PathBindable

object Mappers {
  implicit def dateTimeSlickMapper = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.getMillis), ts => new DateTime(ts.getTime))

  implicit def idSlickMapper[T <: Model[T]] = MappedColumnType.base[Id[T], IdType](_.value, Id[T])

  def idTypeFormMapping: Mapping[models.IdType] = of[UUID](new Formatter[UUID] {
    override val format = Some(("format.uuid", Nil))
    override def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[UUID]
          .either(UUID.fromString(s))
          .left.map(e => Seq(FormError(key, "error.uuid", Nil)))
      }
    }
    override def unbind(key: String, value: UUID) = Map(key -> value.toString)
  })

  def idFormMapping[M]: Mapping[Id[M]] = mapping(
    "value" -> idTypeFormMapping
  )(Id[M] _)(Id.unapply _)

  implicit def idPathBindable[M](implicit longBinder: PathBindable[IdType]) = new PathBindable[Id[M]] {
    def bind(key: String, value: String): Either[String, Id[M]] =
      longBinder.bind(key, value).right map (Id[M](_))
    def unbind(key: String, id: Id[M]): String =
      longBinder.unbind(key, id.value)
  }
  
  trait EnumSlickMapper {
    this: Enumeration =>
    implicit val slickMapping = MappedColumnType.base[Value, Int](_.id, this.apply)
  }

  def enumFormMapping(enum: Enumeration): Mapping[enum.Value] = mapping("value" -> nonEmptyText)(enum.withName(_))(Some(_).map(_.toString))
}

