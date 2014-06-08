package controllers

import controllers.LoginWrapper.loginWrapperForm
import play.api.Application
import play.api.data.Form
import play.api.mvc.Request
import play.api.templates.Html
import securesocial.controllers.DefaultTemplatesPlugin
import securesocial.controllers.Registration.RegistrationInfo

class LoginTemplates(application: Application) extends DefaultTemplatesPlugin(application) {
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], errorMessage: Option[String] = None): Html = {
    views.html.login(
      form.value match {
        case Some((username, password)) =>
          loginWrapperForm.fillAndValidate(LoginWrapperForm(username, password, false))  
        case None =>
          loginWrapperForm
      }
    )(request.flash)
  }

  // override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
  //   securesocial.views.html.Registration.startSignUp(form)
  // }
  
  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.emailSignUp(form, token)(request.flash)
  }

  // override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
  //   securesocial.views.html.Registration.startResetPassword(form)
  // }

  override def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    views.html.resetPasswordPage(form, token)(request.flash)
  }

  // override def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]):Html = {
  //   securesocial.views.html.passwordChange(form)
  // }

  override def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    views.html.main("Not Authorized", Navbar.empty)(
      Html("You are not authorized to access that page."))(request.flash)
  }

  // override def getSignUpEmail(token: String)(implicit flash: Flash): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.signUpEmail(token)))
  // }

  // override def getAlreadyRegisteredEmail(user: Identity)(implicit flash: Flash): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.alreadyRegisteredEmail(user)))
  // }

  // override def getWelcomeEmail(user: Identity)(implicit flash: Flash): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.welcomeEmail(user)))
  // }

  // override def getUnknownEmailNotice()(implicit flash: Flash): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.unknownEmailNotice(request)))
  // }

  // override def getSendPasswordResetEmail(user: Identity, token: String)(implicit flash: Flash): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.passwordResetEmail(user, token)))
  // }

  // override def getPasswordChangedNoticeEmail(user: Identity)(implicit flash: Flash): (Option[Txt], Option[Html]) = {
  //   (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
  // }
}
