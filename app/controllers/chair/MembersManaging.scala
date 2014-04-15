// package controllers.chair

// // import org.joda.time.DateTime
// import controllers.member
// import models.PersonRole
// import models.PersonRole.PersonRole
// import models.Persons
// import models.login.{MyToken, LoginTokens}
// import models.{Email, NewEmail, SentEmails}
// import play.api.data.Form
// import play.api.data.Forms.{list, mapping, nonEmptyText, number}
// import play.api.data.Mapping
// import play.api.mvc.{Action, Controller, Request}
// import securesocial.core.SecureSocial

// case class InvalidateForm(candidates: List[String])
// case class PromoteForm(members: List[Int], newRole: PersonRole)

// object PersonsManaging extends Controller with SecureSocial {
//   val urlTemplateVariable = "@invitationURL"
  
//   val PersonRoleMapping: Mapping[PersonRole] = mapping(
//     "value" -> nonEmptyText)(PersonRole.withName(_))(Some(_).map(_.toString))
  
//   val inviteForm: Form[Email] = Form(
//     Emailing.emailMapping
//   ).fill(Email(
//     null.asInstanceOf[Int], "",
//     "Invitation",
//     "Hello,\n\nThis is an invitation, check out this link:\n\n" + urlTemplateVariable,
//     null.asInstanceOf[DateTime]
//   ))
//   val invalidateForm: Form[InvalidateForm] = Form(
//     mapping(
//       "tokens" -> list(nonEmptyText).verifying("Please select at least one invitation.", _.nonEmpty)
//     )(InvalidateForm.apply _)(InvalidateForm.unapply _)
//   )
//   val promoteForm: Form[PromoteForm] = Form(
//     mapping(
//       "members" -> list(number).verifying("Please select at least one member.", _.nonEmpty),
//       "newRole" -> PersonRoleMapping
//     )(PromoteForm.apply _)(PromoteForm.unapply _)
//   ).fill(PromoteForm(Nil, PersonRole.Chair))
  
//   def page = Action { implicit request =>
//     Ok(views.html.chair.managePersons(inviteForm, invalidateForm, promoteForm))
//   }
  
//   def handle = Action { implicit request => 
//     request.body.asFormUrlEncoded.get("action").headOption match {
//       case Some("invite") => invite
//       case Some("invalidate") => invalidate
//       case Some("promote") => promote
//       case _ => throw new java.lang.UnsupportedOperationException("TODO")
//     }
//   }
  
//   def OkBinded[T](emailF: Option[Form[Email]], invalidateF: Option[Form[InvalidateForm]], promoteF: Option[Form[PromoteForm]])(implicit request: Request[T]) =
//     Ok(views.html.chair.managePersons(
//       emailF getOrElse inviteForm.bindFromRequest.discardingErrors,
//       invalidateF getOrElse invalidateForm.bindFromRequest.discardingErrors, 
//       promoteF getOrElse promoteForm.bindFromRequest.discardingErrors
//     ))

//   def invite[T](implicit request: Request[T]) = {
//     val newInviteForm = inviteForm.bindFromRequest.fold(
//       errors => errors,
//       { case Email(id, to, subject, body, sent) =>
//         val now = DateTime.now
//         SentEmails.ins(NewEmail(to, subject, body, now))
//         to.split(",").foreach { email =>
//           val uuid = IdType.randomIdType().toString
//           val bodyWithLink = body.replaceAll(urlTemplateVariable, member.routes.Dashboard.invite(uuid).absoluteURL())
//           LoginTokens.ins(MyToken(uuid, email, now, now.plusDays(7), false, true))
//           Emailing.sendEmail(email, subject, bodyWithLink)
//         }
//         inviteForm
//       }
//     )
//     OkBinded(Some(newInviteForm), None, None)
//   }
  
//   def invalidate[T](implicit request: Request[T]) = {
//     val newInvalidateForm = invalidateForm.bindFromRequest.fold(
//       errors => errors,
//       { case InvalidateForm(candidates) =>
//         candidates.foreach(LoginTokens.del)
//         invalidateForm
//       }
//     )
//     OkBinded(None, Some(newInvalidateForm), None)
//   }
  
//   def promote[T](implicit request: Request[T]) = {
//     val newPromoteForm = promoteForm.bindFromRequest.fold(
//       errors => errors,
//       { case PromoteForm(members, newRole) =>
//         play.api.Logger.info(members + " w " + newRole)
//         members.foreach(Persons.promote(_, newRole))
//         promoteForm
//       }
//     )
//     OkBinded(None, None, Some(newPromoteForm))
//   }
// }
