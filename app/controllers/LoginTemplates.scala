package controllers

import play.api.mvc.{RequestHeader, Request}
import play.api.templates.{Html, Txt}
import play.api.{Logger, Plugin, Application}
import securesocial.core.{Identity, SecuredRequest, SocialUser}
import play.api.data.Form
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.DefaultTemplatesPlugin
import controllers.LoginWrapper.loginWrapperForm

class LoginTemplates(application: Application) extends DefaultTemplatesPlugin(application) {
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String] = None): Html = {
    views.html.login.modal(
      form.value match {
        case Some((username, password)) => loginWrapperForm.fillAndValidate(LoginWrapperForm(username, password, false))  
        case None => loginWrapperForm
      }, msg)
  }

  // override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
  //   securesocial.views.html.Registration.signUp(form, token)
  // }

  // override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
  //   securesocial.views.html.Registration.startSignUp(form)
  // }

  // override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
  //   securesocial.views.html.Registration.startResetPassword(form)
  // }

  // override def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
  //   securesocial.views.html.Registration.resetPasswordPage(form, token)
  // }

  // override def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]):Html = {
  //   securesocial.views.html.passwordChange(form)
  // }

  // override def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
  //   securesocial.views.html.notAuthorized()
  // }

  // override def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.signUpEmail(token)))
  // }

  // override def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.alreadyRegisteredEmail(user)))
  // }

  // override def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.welcomeEmail(user)))
  // }

  // override def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.unknownEmailNotice(request)))
  // }

  // override def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.passwordResetEmail(user, token)))
  // }

  // override def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
  // }
}
