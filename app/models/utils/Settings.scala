package models.utils

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

/** This file holds all the code related to the storage of SlickChair Settings
  * in the database. Settings aims to be accessed via the provided singleton
  * objects get() and set(.) methods.
  */

/** Generic trait for Settings. The Settings table should only be accessed
  * using the get() and set(.) methods defined in this trait.
  *
  * @tparam T  type of the setting
  */
trait Setting[T] {
  // Hack. (I have no idea how portable this is)
  /**
    * Com
    */
  private def dbname: String = this.getClass.getName.split("$")(0)
  protected def fromString(s: String): T
  def default: T
  def description: String
  
  def get: T = DB.withSession{implicit session =>
    Query(Settings).filter(_.name is dbname).list.headOption match {
      case Some(DBSetting(_, value)) => fromString(value)
      case None => default
    }
  }
  def set(value: T): Unit = DB.withTransaction { implicit session =>
    val entry = DBSetting(dbname, value.toString)
    Query(Settings).filter(_.name is dbname).list.headOption match {
      // Insert-or-update would have been nice here, https://github.com/slick/slick/issues/6.
      case Some(_) => Settings.filter(_.name is dbname).update(entry)
      case None => Settings.insert(entry)
    }
  }
}

case class BooleanSetting(default: Boolean, description: String) extends Setting[Boolean] {
  override def fromString(s: String) = s.toBoolean
}
case class IntSetting(default: Int, description: String) extends Setting[Int] {
  override def fromString(s: String) = s.toInt
}
case class StringSetting(default: String, description: String) extends Setting[String] {
  override def fromString(s: String) = s
}

object SubmissionLock extends BooleanSetting(
  false, "Prevent user from posting new submissions")
// CanMakeSubmission
// CanEditSubmission
// CanWithdrawSubmission
// CanViewStatus
// CanMemberSignUp
// CanMemberLogIn
// CanBid
// CanUploadReviewPaper
// CanUploadFinalPaper
object ConferenceShortName extends StringSetting(
  "SlickChair Demo", "Short name of the conference (first entry of the menu)."
)

case class DBSetting (name: String, value: String)

object Settings extends Table[DBSetting]("SETTINGS") {
  def name = column[String]("NAME", O.DBType("TEXT"), O.PrimaryKey)
  def value = column[String]("VALUE", O.DBType("TEXT"))
  def * = name ~ value <> (DBSetting, DBSetting.unapply _)
}