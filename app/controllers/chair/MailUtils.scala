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
import controllers.chair.MailUtils._

object MailUtils {
  val fromAddress = current.configuration.getString("smtp.from").get

  val emailMapping: Mapping[Email] = mapping(
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
}