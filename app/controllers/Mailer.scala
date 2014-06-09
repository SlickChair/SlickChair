package controllers

import com.typesafe.plugin._
import models.Email
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import scala.concurrent.duration._

object Mailer {
 def send(email: Email): Unit = {
   Akka.system.scheduler.scheduleOnce(1.seconds) {
     val from = current.configuration.getString("smtp.from").get
     val mail = use[MailerPlugin].email
     mail.setSubject(email.subject)
     mail.setRecipient(email.to.split(","): _*)
     mail.setFrom(from)
     mail.send(email.content)
   }
   ()
 }
}
