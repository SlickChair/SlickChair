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
import models.secureSocial._

object Emailing extends Controller with SecureSocial {
  val emailForm = Form[Email] (MailUtils.emailMapping)
  
  def form = Action(Ok(views.html.email(None, emailForm)))
  
  def send = Action { implicit request =>
    emailForm.bindFromRequest.fold(
      errors => Ok(views.html.email(None, errors)),
      filledForm => {
        filledForm.to.split(",").foreach { email =>
          SecureSocialUsers.withEmail(email).getOrElse{ throw new java.lang.UnsupportedOperationException("TODO") }
          // TODO add email variables like 
          // @title
          // @firstname
          // @lastname
        }
        SentEmails.ins(NewEmail(
          filledForm.to,
          filledForm.subject,
          filledForm.body,
          DateTime.now
        ))
        filledForm.to.split(",").foreach { email =>
          val user = SecureSocialUsers.withEmail(email).get
          MailUtils.sendEmail(user.email, filledForm.subject, filledForm.body)
        }
        Ok(views.html.email(Some("Email sent)."), emailForm))
      }
    )
  }
}