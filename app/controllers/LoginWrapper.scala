package controllers

import play.api.data.Form
import play.api.data.Forms.{boolean, default, mapping, nonEmptyText, text}
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import securesocial.controllers.{ProviderController, Registration}
import securesocial.core.{SecureSocial, UserService}
import securesocial.core.providers.UsernamePasswordProvider.UsernamePassword
import securesocial.core.providers.utils.Mailer

case class LoginWrapperForm(
  username: String,
  email: String,
  password: String,
  create: Boolean
)

/** Wraps the four SecureSocial events into a single handler. Login, signup,
  * change password and forget password are all called using the
  * loginWrapperForm form.
  */
object LoginWrapper extends Controller with SecureSocial {
  /** "Extends" securesocial.core.providers.UsernamePasswordProvider.loginForm */
  val loginWrapperForm = Form[LoginWrapperForm] (mapping(
    "username" -> nonEmptyText,
    "password" -> default(text, ""),
    "create" -> boolean
  )((username, password, create) => LoginWrapperForm(username, username, password, create))
   (ssw => Some(ssw.username, ssw.password, ssw.create) )
  )
  
  /** Dispatches to the appropriate action as depending of the `create` field
    *  and the existence of the user in the database.
    */
  def dispatch = Action { implicit request =>
        import play.api.Logger
    loginWrapperForm.bindFromRequest.fold(
      errors => Ok("errors: " + errors), // TODO, show in src form? BadRequest(...)
      form => {
        if(!form.create) {
          ProviderController.authenticateByPost(UsernamePassword)(request) // Login
        } else {
          // Because of the way securesocial implements the forms for forgot
          // password, creat account and login it is not possible to use the
          // same request for the three actions. The forgot password and
          // create account code comes from controllers/Registration.scala.
          UserService.findByEmailAndProvider(form.username, UsernamePassword) match {
            case Some(user) => 
              // Forgot password
              val token = createToken(form.email, isSignUp = false)
              Mailer.sendPasswordResetEmail(user, token._1)
            case None =>
              // Create account
              val token = createToken(form.email, isSignUp = true)
              Mailer.sendSignUpEmail(form.email, token._1)
          }
          Redirect(Registration.onHandleStartResetPasswordGoTo).flashing(Registration.Success -> Messages(Registration.ThankYouCheckEmail))
        }
      }
    )
  }
  
  /** Copy pasted from securesocial.controllers/Registration.scala, not usable
    * from here because of the private modifier...
    */
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
  
// Fun fact: compiling this method results in 
// [error] (compile:compile) java.lang.NullPointerException
// def dispatch = Action { implicit request =>
//   import com.typesafe.plugin._
//   import play.api.Play.current
//   SecureSocial.withRefererAsOriginalUrl(Ok(use[LoginTemplates].getLoginPage(request, UsernamePasswordProvider.loginForm)))
// }
}

