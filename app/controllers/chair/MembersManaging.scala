package controllers.chair

import concurrent.duration.DurationInt
import com.typesafe.plugin.{MailerPlugin, use}
import play.api.Play.current
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import securesocial.core.SecureSocial
import com.github.tototoshi.slick.JodaSupport._
import java.sql.Date
import org.joda.time.DateTime
import models.utils._
import models.entities._
import models.entities.MemberRole._
import models.secureSocial._
import java.util.UUID
import controllers._
import controllers.chair._
import controllers.member.Reviewing._

case class InvalidateForm(candidates: List[String])
case class PromoteForm(members: List[Int], newRole: MemberRole)

object MembersManaging extends Controller with SecureSocial {
  val urlTemplateVariable = "@invitationURL"
  
  val memberRoleMapping: Mapping[MemberRole] = mapping(
    "value" -> nonEmptyText)(MemberRole.withName(_))(Some(_).map(_.toString))
  
  val inviteForm: Form[Email] = Form(
    Emailing.emailMapping
  ).fill(Email(
    null.asInstanceOf[Int], "",
    "Invitation",
    "Hello,\n\nThis is an invitation, check out this link:\n\n" + urlTemplateVariable,
    null.asInstanceOf[DateTime]
  ))
  val invalidateForm: Form[InvalidateForm] = Form(
    mapping(
      "tokens" -> list(nonEmptyText).verifying("Please select at least one invitation.", _.nonEmpty)
    )(InvalidateForm.apply _)(InvalidateForm.unapply _)
  )
  val promoteForm: Form[PromoteForm] = Form(
    mapping(
      "members" -> list(number).verifying("Please select at least one member.", _.nonEmpty),
      "newRole" -> memberRoleMapping
    )(PromoteForm.apply _)(PromoteForm.unapply _)
  ).fill(PromoteForm(Nil, MemberRole.Chair))
  
  def page = Action(Ok(views.html.manageMembers(inviteForm, invalidateForm, promoteForm)))
  
  def handle = Action { implicit request => 
    request.body.asFormUrlEncoded.get("action").headOption match {
      case Some("invite") => invite
      case Some("invalidate") => invalidate
      case Some("promote") => promote
      case _ => throw new java.lang.UnsupportedOperationException("TODO")
    }
  }
  
  def OkBinded[T](emailF: Option[Form[Email]], invalidateF: Option[Form[InvalidateForm]], promoteF: Option[Form[PromoteForm]])(implicit request: Request[T]) =
    Ok(views.html.manageMembers(
      emailF getOrElse inviteForm.bindFromRequest.discardingErrors,
      invalidateF getOrElse invalidateForm.bindFromRequest.discardingErrors, 
      promoteF getOrElse promoteForm.bindFromRequest.discardingErrors
    ))

  def invite[T](implicit request: Request[T]) = {
    val newInviteForm = inviteForm.bindFromRequest.fold(
      errors => errors,
      { case Email(id, to, subject, body, sent) =>
        val now = DateTime.now
        SentEmails.ins(NewEmail(to, subject, body, now))
        to.split(",").foreach { email =>
          val uuid = UUID.randomUUID().toString
          val bodyWithLink = body.replaceAll(urlTemplateVariable, member.routes.Dashboard.invite(uuid).absoluteURL())
          SecureSocialTokens.ins(MyToken(uuid, email, now, now.plusDays(7), false, true))
          Emailing.sendEmail(email, subject, bodyWithLink)
        }
        inviteForm
      }
    )
    OkBinded(Some(newInviteForm), None, None)
  }
  
  def invalidate[T](implicit request: Request[T]) = {
    val newInvalidateForm = invalidateForm.bindFromRequest.fold(
      errors => errors,
      { case InvalidateForm(candidates) =>
        candidates.foreach(SecureSocialTokens.del)
        invalidateForm
      }
    )
    OkBinded(None, Some(newInvalidateForm), None)
  }
  
  def promote[T](implicit request: Request[T]) = {
    val newPromoteForm = promoteForm.bindFromRequest.fold(
      errors => errors,
      { case PromoteForm(members, newRole) =>
        play.api.Logger.info(members + " w " + newRole)
        members.foreach(Members.promote(_, newRole))
        promoteForm
      }
    )
    OkBinded(None, None, Some(newPromoteForm))
  }
}