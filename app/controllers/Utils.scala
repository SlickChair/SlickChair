package controllers

import java.util.UUID
import models.PersonRole._
import models.{ User, Persons, Person, LoginUsers, Model, Id }
import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.data.Forms._
import play.api.data.{ FormError, Mapping }
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{ DB, SlickExecutionContext }
import play.api.i18n.Messages
import play.api.mvc.BodyParsers.parse.anyContent
import play.api.mvc.{ Action, Request, SimpleResult, Results, BodyParser, WrappedRequest }
import play.api.Play.current
import scala.concurrent.{ ExecutionContext, Future }
import securesocial.core.providers.utils.RoutesHelper
import securesocial.core.{ IdentityProvider, SecureSocial, SecuredRequest, Authenticator, UserService }
import play.api.data.Forms.ignored
import org.joda.time.DateTime
import models._

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
  
  def shorten(id: IdType): String = id.toString.take(4).toUpperCase
  
  /** Semantically, curse values need to be set when handling the POST on a
    * form before storing the Paper in the database... This could be made
    * type safe by using new case classes for forms. */
  def curse[T]: Mapping[T] = ignored(null.asInstanceOf[T])

  def enumMapping(enum: Enumeration): Mapping[enum.Value] = mapping("value" -> nonEmptyText)(enum.withName(_))(Some(_).map(_.toString))
  
  def idMapping[M <: Model[M]]: Mapping[Id[M]] = mapping(
    "value" -> uuid
  )(Id[M] _)(Id.unapply _)

  case class SlickRequest[A](
    dbSession: Session,
    dbExecutionContext: ExecutionContext,
    user: Person,
    request: Request[A],
    now: DateTime = new DateTime()
  ) extends WrappedRequest[A](request)

  implicit def slickRequestAsSession[_](implicit r: SlickRequest[_]): Session = r.dbSession
  implicit def slickRequestAsExecutionContext[_](implicit r: SlickRequest[_]): ExecutionContext = r.dbExecutionContext
  
  /** Custom mix between securesocial.core.SecureSocial and play.api.db.slick.DBAction */
  object SlickAction {
    def apply[A](isAuthorized: Authorization, bodyParser: BodyParser[A] = anyContent)(requestHandler: SlickRequest[A] => SimpleResult): Action[A] = {
      
      val minConnections = 5
      val maxConnections = 5
      val partitionCount = 2
      val maxQueriesPerRequest = 20
      val (executionContext, threadPool) = SlickExecutionContext.threadPoolExecutionContext(minConnections, maxConnections)

      if(threadPool.getQueue.size() >= maxConnections * maxQueriesPerRequest) 
        Action(bodyParser) { _ => Results.ServiceUnavailable }
      else {
        Action.async(bodyParser) { implicit request =>
          Future {
   
            DB withSession { implicit session =>
              getUser match {
                case Some(user) =>
                  val slickRequest = SlickRequest(session, executionContext, user, request)
                  if (isAuthorized(slickRequest))
                    requestHandler(slickRequest)
                  else
                    Results.Redirect(RoutesHelper.notAuthorized.absoluteURL(IdentityProvider.sslEnabled))
                case None =>
                  Results.Redirect(RoutesHelper.login().absoluteURL(IdentityProvider.sslEnabled))
                    .flashing("error" -> Messages("securesocial.loginRequired"))
                    .withSession(request.session + (SecureSocial.OriginalUrlKey -> request.uri))
                    .discardingCookies(Authenticator.discardingCookie)
              }
            }
          }(executionContext)
        }
      }
    }
    
    private def getUser[A](implicit session: Session, request: Request[A]): Option[Person] = {
      for (
        authenticator <- SecureSocial.authenticatorFromRequest ;
        secureSocialUser <- LoginUsers.UserByidentityId(authenticator.identityId).firstOption.map(_.toIdentity)
      ) yield {
        Authenticator.save(authenticator.touch)
        Persons.withEmail(secureSocialUser.email.get)
      }
    }
  }
}
