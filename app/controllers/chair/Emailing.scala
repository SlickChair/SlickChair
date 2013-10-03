package controllers.chair

import concurrent.duration.DurationInt
import org.joda.time.DateTime
import com.typesafe.plugin.{MailerPlugin, use}
import models.login.LoginUsers
import models.utils.{Email, NewEmail, SentEmails}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, nonEmptyText}
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import securesocial.core.SecureSocial

object Emailing extends Controller with SecureSocial {
  val fromAddress = current.configuration.getString("smtp.from").get
  
  val emailMapping = mapping(
    // TODO: email validation + @firstname validation
    "id" -> ignored(null.asInstanceOf[Int]),
    "to" -> nonEmptyText,
    "subject" -> nonEmptyText,
    "body" -> nonEmptyText,
    "sent" -> ignored(null.asInstanceOf[DateTime])
  )(Email.apply _)(Email.unapply _)
  
  def sendEmail(to: String, subject: String, body: String) {
    Akka.system.scheduler.scheduleOnce(1 seconds) {
      val mail = use[MailerPlugin].email
      mail.addRecipient(to)
      mail.setSubject(subject)
      mail.addFrom(fromAddress)
      mail.send(body, "")
    }
  }
  
  val emailForm = Form[Email] (emailMapping)
  
  def form = Action(Ok(views.html.chair.email(None, emailForm)))
  
  def send = Action { implicit request =>
    emailForm.bindFromRequest.fold(
      errors => Ok(views.html.chair.email(None, errors)),
      { case Email(id, to, subject, body, sent) =>
        to.split(",").foreach { email =>
          LoginUsers.withEmail(email).getOrElse{ throw new java.lang.UnsupportedOperationException("TODO") }
          // TODO add email variables like 
          // @title
          // @firstname
          // @lastname
        }
        SentEmails.ins(NewEmail(to, subject, body, DateTime.now))
        to.split(",").foreach { email =>
          val user = LoginUsers.withEmail(email).get
          Emailing.sendEmail(user.email, subject, body)
        }
        Ok(views.html.chair.email(Some("Email sent)."), emailForm))
      })
  }
}
