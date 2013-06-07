package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models._

object ReviewerRole extends Enumeration with BitmaskedEnumeration {
  type ReviewerRole = Value
  val Chair, Reviewer, SubReviewer = Value
}
import ReviewerRole._

// Reviewers account info
case class Reviewer(
  id: Int,
  // securesocialid: UserId(String, String),
  role: ReviewerRole,
  firstname: String,
  lastname: String,
  organization: Option[String],
  positiontitle: Option[String],
  email: String
)

object Reviewers extends Table[Reviewer]("REVIEWERS"){
  def id = column[Int]("id", O.AutoInc)
  // def securesocialid = column[UserId(String, String) FOREIGN_KEY]("securesocialid")
  def role = column[ReviewerRole]("role")
  def firstname = column[String]("firstname")
  def lastname = column[String]("lastname")
  def organization = column[Option[String]]("organization")
  def positiontitle = column[Option[String]]("positiontitle")
  def email = column[String]("email")
  
  def pk = primaryKey("reviewers_pk", id)
  
  def * = id ~ role ~ firstname ~ lastname ~ organization ~ positiontitle ~ email <> (Reviewer.apply _, Reviewer.unapply _)
}