package controllers

import models.entities.{Author, Authors, NewPaper, Paper, PaperType}
import models.entities.{Papers, Topics}
import models.entities.PaperType.PaperType
import models.utils.{Files, NewFile}
import models.relations.{PaperTopics, PaperTopic}
import models.login.User
import play.api.data.Form
import play.api.data.Forms.{boolean, ignored, list, mapping, nonEmptyText, number, text, default}
import play.api.data.Mapping
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Action, Controller}
import securesocial.core.{SecureSocial, UserService}
import securesocial.controllers.{ProviderController, Registration}

case class LoginWrapperForm(
  username: String,
  email: String,
  password: String,
  create: Boolean
)

/** Wraps the four SecureSocial events into a single handler. Login, signup,
  * change password and forget password are all called using a single form.
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
    
  // val submissionForm: Form[SubmissionForm] = Form(
  //   mapping(
  //     "paper" -> paperMapping,
  //     "nauthors" -> number,
  //     "authors" -> list(authorMapping),
  //     "topics" -> list(number).verifying("Please select at least one topic.", _.nonEmpty)
  //   )((paper, nauthors, authors, topics) =>
  //       SubmissionForm(paper, authors.take(nauthors).zipWithIndex.map{case (a, i) => a.copy(position = i)}, topics))
  //     // After calling this function authors positions are constistant but
  //     // authors paperid and paper {id, contactemail, submissiondate,
  //     // lastupdate, accepted, fileid} are still null.
  //    (submissionForm =>
  //       Some(submissionForm.paper, submissionForm.authors.size, submissionForm.authors, submissionForm.topics))
  // )
  
  
  /** Dispatches the request to the appropriate SecureSocial handler. */
  def dispatch = Action { implicit request =>
        import play.api.Logger
    loginWrapperForm.bindFromRequest.fold(
      errors => Ok("errors: " + errors), // TODO, show in src form?
      form => {
        if(!form.create) {
          // Login.
          ProviderController.authenticateByPost("userpass")(request)
        } else {
          // Forgot password or creat account. In order to re-use SecureSocial
          // internals we need to remap the request so that it defines an
          // "email" field, instead of "username" that used for the login.
          val withEmailRequest = request.map {
             // I don't see another was to do it...
            case body: Map[_, _] => 
              Logger.info("map:)")
              // val typedBody = body.asInstanceOf[Map[String, Seq[String]]]

              // Logger.info(typedBody.toString)
              // val updatedBody = typedBody + (("email", Seq(form.username)))
              // Logger.info(updatedBody.toString)
              // println (updatedBody)
              // updatedBody.asInstanceOf[AnyContent]
              AnyContentAsEmpty
            case body => 
              Logger.info("other:(" + body.getClass)
              AnyContentAsEmpty
          }
          
          UserService.findByEmailAndProvider(form.username, "userpass") match {
            case Some(user) =>
              // Forgot password.
              Logger.info("handleStartResetPassword")
              Registration.handleStartResetPassword(withEmailRequest)
            case None =>
              // Creat account.
              Logger.info("handleStartSignUp")
              Registration.handleStartSignUp(withEmailRequest)
          }
        }
      }
    )
  }
  
// Fun fact: compiling this method results in 
// [error] (compile:compile) java.lang.NullPointerException
// def dispatch = Action { implicit request =>
//   import com.typesafe.plugin._
//   import play.api.Play.current
//   SecureSocial.withRefererAsOriginalUrl(Ok(use[LoginTemplates].getLoginPage(request, UsernamePasswordProvider.loginForm)))
// }
}

