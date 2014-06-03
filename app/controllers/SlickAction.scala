package controllers

import concurrent.{ExecutionContext, Future}
import models._
import play.api.Play.current
import play.api.db.slick.{DB, SlickExecutionContext}
import play.api.db.slick.Config.driver.simple._
import play.api.i18n.Messages
import play.api.mvc.{Action, BodyParser}
import play.api.mvc.{Request, Results, SimpleResult, WrappedRequest}
import play.api.mvc.BodyParsers.parse.anyContent
import securesocial.core.{Authenticator, IdentityProvider, SecureSocial}
import securesocial.core.providers.utils.RoutesHelper
import models.Query

case class SlickRequest[A](
  dbSession: Session,
  dbExecutionContext: ExecutionContext,
  user: Person,
  request: Request[A]
) extends WrappedRequest[A](request) {
  val connection = Connection(dbSession)
  val db = connection.database()
}

/** Custom mix between securesocial.core.SecureSocial and play.api.db.slick.DBAction */
object SlickAction {
  implicit def slickRequestAsExecutionContext[_](implicit r: SlickRequest[_]): ExecutionContext = r.dbExecutionContext

  def apply[A](isAuthorized: Authorization, isEnabled: Configuration => Boolean, bodyParser: BodyParser[A] = anyContent)(requestHandler: SlickRequest[A] => SimpleResult): Action[A] = {
    
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
                if(isAuthorized(slickRequest)) {
                  if(isEnabled(Query(Connection(session).database).configuration))
                    requestHandler(slickRequest)
                  else
                    Results.Redirect(routes.Submitting.disabled)
                }
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
      Query(Connection(session).database).personWithEmail(secureSocialUser.email.get)
    }
  }
}
