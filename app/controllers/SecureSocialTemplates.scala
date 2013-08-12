package controllers

import play.api.Application
import play.api.data.Form
import play.api.mvc.{Request, RequestHeader}
import play.api.templates.{Html, Txt}
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.TemplatesPlugin
import securesocial.core.{Identity, SecuredRequest}

class SecureSocialTemplates(application: Application) extends TemplatesPlugin {
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String] = None): Html =
  Html("""
    <!DOCTYPE HTML>
    <html lang="en-US">
        <head>
            <meta charset="UTF-8">
            <meta http-equiv="refresh" content="0;url=http://localhost:9000/authenticate/google">
            <script type="text/javascript">
                window.location.href = "http://localhost:9000/authenticate/google"
            </script>
            <title>Page Redirection</title>
        </head>
        <body>
            <!-- Note: don't tell people to `click` the link, just tell them that it is a link. -->
            If you are not redirected automatically, follow the <a href='http://example.com'>link to example</a>
        </body>
    </html>
  """)
    // views.html.securesocialtemplates.login(form, msg)

  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = 
    views.html.securesocialtemplates.Registration.signUp(form, token)

  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = 
    views.html.securesocialtemplates.Registration.startSignUp(form)

  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = 
    views.html.securesocialtemplates.Registration.startResetPassword(form)

  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = 
    views.html.securesocialtemplates.Registration.resetPasswordPage(form, token)

  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]):Html = 
    views.html.securesocialtemplates.passwordChange(form)

  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = 
    views.html.securesocialtemplates.notAuthorized()

  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = 
    (None, Some(views.html.securesocialtemplates.mails.signUpEmail(token)))

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = 
    (None, Some(views.html.securesocialtemplates.mails.alreadyRegisteredEmail(user)))

  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = 
    (None, Some(views.html.securesocialtemplates.mails.welcomeEmail(user)))

  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = 
    (None, Some(views.html.securesocialtemplates.mails.unknownEmailNotice(request)))

  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = 
    (None, Some(views.html.securesocialtemplates.mails.passwordResetEmail(user, token)))

  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = 
    (None, Some(views.html.securesocialtemplates.mails.passwordChangedNotice(user)))
}
