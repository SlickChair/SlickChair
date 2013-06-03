/*import play.api.Application
import play.api.GlobalSettings
import play.api.Play.current
import play.api.db.DB

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    lazy val database = Database.forDataSource(DB.getDataSource())

    database withSession {
      Tasks.ddl.create
    }
  }
}*/