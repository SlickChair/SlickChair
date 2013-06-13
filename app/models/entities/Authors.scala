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
  email: String
)

object Authors extends Table[Author]("AUTHORS") {
  def paperid = column[Int]("PAPERID")
  def position = column[Int]("POSITION")
  def firstname = column[String]("FIRSTNAME")
  def lastname = column[String]("LASTNAME")
  def organization = column[String]("ORGANIZATION")
  def email = column[String]("EMAIL")
  
  def pk = primaryKey("AUTHORS_PK", paperid ~ position)
  def paper = foreignKey("AUTHORS_PAPERID_FK", paperid, Papers)(_.id)
  def * = paperid ~ position ~ firstname ~ lastname ~ organization ~ email <> (Author.apply _, Author.unapply _)

  def all = DB.withSession(implicit session =>
    Query(Authors).list )
  
  def of(id: Int) = DB.withSession(implicit session =>
    Query(Authors).filter(_.paperid is id).list )
  
  def ins(author: Author) = DB.withSession{implicit session =>
    play.api.Logger.info("ins " + author)

    Authors.insert(author) }
  
  def del(id: Int) = DB.withSession{implicit session =>
    play.api.Logger.info("del " + id)

    Authors.filter(_.paperid is id).delete }
}