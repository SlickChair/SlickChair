package controllers

import java.util.UUID
import models.User
import models.{ Persons, Person }
import models.PersonRole._
import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.data.Forms._
import play.api.data.{ FormError, Mapping }
import play.api.db.slick.Config.driver.simple._
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import securesocial.core.{ Authorization, Identity, RequestWithUser, SecuredRequest }

object Utils {
  /** Source: https://github.com/guardian/deploy/blob/master/riff-raff/app/utils/Forms.scala */
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

  // def uEmail()(implicit request: SecuredRequest[_]): String = request.user.email.get
  def getUser()(implicit request: SecuredRequest[_], s: Session): Person = Persons.withEmail(request.user.email.get)

  /** Authorization checking that the user is chair. */
  case class WithRole(role: PersonRole) extends Authorization {
    def isAuthorized(user: Identity): Boolean =
      false
      // Persons.withEmail(user.email.get).filter(_.role == PersonRole.Chair).nonEmpty TODO
  }
}
