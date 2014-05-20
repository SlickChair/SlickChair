package controllers

import java.util.UUID
import concurrent.Await
import concurrent.duration.Duration
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms.{boolean, default, mapping, nonEmptyText, text}
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller, Request}
import securesocial.controllers.{ProviderController, Registration}
import securesocial.core.{Identity, SecureSocial, UserService}
import securesocial.core.providers.Token
import securesocial.core.providers.UsernamePasswordProvider.UsernamePassword
import securesocial.core.providers.utils.Mailer

case class LoginWrapperForm(
  username: String,
  password: String,
  create: Boolean
)

/** Wraps the four SecureSocial events into a single handler. Login, signup,
  * change password and forget password are all called using the
  * loginWrapperForm form. */
object LoginWrapper extends Controller with SecureSocial {
  /** "Extends" securesocial.core.providers.UsernamePasswordProvider.loginForm */
  val loginWrapperForm = Form[LoginWrapperForm] (mapping(
    "username" -> nonEmptyText,
    "password" -> default(text, ""),
    "create" -> boolean
  )(LoginWrapperForm.apply _)(LoginWrapperForm.unapply _))
  
  /** Dispatches to the appropriate action as depending of the `create` field
    * and the existence of the user in the database. */
  def dispatch = Action { implicit request =>
    loginWrapperForm.bindFromRequest.fold(
      errors => Ok(views.html.login(errors)),
      form => {
        if(!form.create) {
          val req = ProviderController.authenticateByPost(UsernamePassword)(request)
          Await.result(req, Duration.Inf)
        } else {
          UserService.findByEmailAndProvider(form.username, UsernamePassword) match {
            case Some(user) => forgot(form, user)
            case None => signup(form)
          }
        }
      }
    )
  }
  
  // Because of the way securesocial implements the forms for forgot
  // password, creat account and login it is not possible to use the
  // same request for the three actions. The forgot password and
  // create account code comes from controllers/Registration.scala.
  private def thankYouCheckEmail = {
    Redirect(Registration.onHandleStartResetPasswordGoTo).flashing(
      Registration.Success -> Messages(Registration.ThankYouCheckEmail))
  }
  
  private def forgot(form: LoginWrapperForm, user: Identity)(implicit request: Request[_]) = {
    val token = createToken(form.username, isSignUp = false)
    Mailer.sendPasswordResetEmail(user, token._1)
    thankYouCheckEmail
  }
  
  private def signup(form: LoginWrapperForm)(implicit request: Request[_]) = {
    val token = createToken(form.username, isSignUp = true)
    Mailer.sendSignUpEmail(form.username, token._1)
    thankYouCheckEmail
  }
    
  /** Copy pasted from securesocial.controllers/Registration.scala, not usable
    * from here because of the private modifier... */
  import java.util.UUID
  import org.joda.time.DateTime
  import securesocial.core.providers.Token
  private def createToken(email: String, isSignUp: Boolean): (String, Token) = {
    
    val uuid = UUID.randomUUID().toString
    val now = DateTime.now

    val token = Token(
      uuid, email,
      now,
      now.plusMinutes(Registration.TokenDuration),
      isSignUp = isSignUp
    )
    UserService.save(token)
    (uuid, token)
  }
}
