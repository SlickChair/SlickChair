package controllers

import play.api.data.{ FormError, Mapping }
import java.util.UUID
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.format.Formats._

/** Source: https://github.com/guardian/deploy/blob/master/riff-raff/app/utils/Forms.scala */
object Utils {
  val uuid: Mapping[UUID] = of[UUID](new Formatter[UUID] {
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
}
