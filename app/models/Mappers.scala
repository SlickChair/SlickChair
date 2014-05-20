package models

import BidValue._
import java.nio.ByteBuffer
import java.sql.Timestamp
import java.util.UUID
import models._
import models.PersonRole._
import org.joda.time.DateTime
import PaperType._
import PersonRole._
import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.data.Forms._
import play.api.data.Forms.ignored
import play.api.data.{ FormError, Mapping }
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{ DB, SlickExecutionContext }
import play.api.i18n.Messages
import play.api.mvc.BodyParsers.parse.anyContent
import play.api.mvc.{ PathBindable, Action, Request, SimpleResult, Results, BodyParser, WrappedRequest }
import play.api.Play.current
import ReviewConfidence._
import ReviewEvaluation._
import scala.concurrent.{ ExecutionContext, Future }
import securesocial.core.providers.utils.RoutesHelper
import securesocial.core.{ IdentityProvider, SecureSocial, SecuredRequest, Authenticator, UserService }

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

