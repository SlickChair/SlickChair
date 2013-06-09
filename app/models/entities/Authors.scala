package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Authors for each submission
case class Author(
  paperid: Int,
  position: Int,
  firstname: String,
  lastname: String,
  organization: String,
  positiontitle: String,
  email: String
)

object Authors extends Table[Author]("AUTHORS") {
  def paperid = column[Int]("paperid")
  def position = column[Int]("position")
  def firstname = column[String]("firstname")
  def lastname = column[String]("lastname")
  def organization = column[String]("organization")
  def positiontitle = column[String]("positiontitle")
  def email = column[String]("email")
  
  def pk = primaryKey("authors_pk", paperid ~ position)
  def paper = foreignKey("authors_paperid_fk", paperid, Papers)(_.id)
  
  def * = paperid ~ position ~ firstname ~ lastname ~ organization ~ positiontitle ~ email <> (Author.apply _, Author.unapply _)
}