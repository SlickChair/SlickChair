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
import controllers.chair._

case class ManageMembersForm(
  invite: Email,
  pendingInvitations: List[Int],
  promotedMembers: List[Int],
  newRole: MemberRole
)

object ManageMembers extends Controller with SecureSocial {
  val urlTemplateVariable = "@invitationURL"
  
  val memberRoleMapping: Mapping[MemberRole] = mapping(
    "value" -> nonEmptyText)(MemberRole.withName(_))(Some(_).map(_.toString))
  
  val emailForm = Form[Email] (
    MailUtils.emailMapping
  ).fill(Email(
    null.asInstanceOf[Int], "",
    "Invitation",
    "Hello,\n\nThis is an invitation, check out this link: " + urlTemplateVariable,
    null.asInstanceOf[DateTime]
  ))
  val invalidateForm = Form[List[Int]] (
    "candidates" -> list(number)
  )
  val promoteForm = Form[(List[Int], MemberRole)] (
    tuple(
      "promotedMembers" -> list(number),
      "newRole" -> memberRoleMapping
    )
  ).fill(Nil, MemberRole.Chair)
  
  
  val manageMembersForm = Form[ManageMembersForm] (
    mapping(
      "invite" -> MailUtils.emailMapping,
      "pendingInvitations" -> list(number),
      "promotedMembers" -> list(number),
      "newRole" -> memberRoleMapping
    )(ManageMembersForm.apply _)(ManageMembersForm.unapply _)
  ).fill(ManageMembersForm(
    Email(
      null.asInstanceOf[Int], "",
      "Invitation",
      "Hello,\n\nThis is an invitation, check out this link: " + urlTemplateVariable,
      null.asInstanceOf[DateTime]),
    Nil, Nil, MemberRole.Chair
  ))
  
  private def createToken(email: String, isSignUp: Boolean): (String, MyToken) = {
    val uuid = UUID.randomUUID().toString
    val now = DateTime.now

    val token = MyToken(
      UUID.randomUUID().toString,
      email,
      DateTime.now,
      DateTime.now.plusDays(7),
      false,
      true
    )
    SecureSocialTokens.ins(token)
    (uuid, token)
  }
  
  // private def okWith(form: Form[ManageMembersForm]) = Ok(views.html.manageMembers(
  //   SecureSocialTokens.allInvitations,
    
  //   Papers.relevantCategories ::: Members.relevantCategories,
  //   Templates.all,
  //   form
  // ))
  
  def page = Action(Ok(views.html.manageMembers(manageMembersForm)))

  def invite = TODO
  def invalidate = TODO
  def promote = TODO
}