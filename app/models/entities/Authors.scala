package models.entities

import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/** This file holds all the code related to the storage of submission Authors
  * in the database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

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
  def firstname = column[String]("FIRSTNAME", O.DBType("TEXT"))
  def lastname = column[String]("LASTNAME", O.DBType("TEXT"))
  def organization = column[String]("ORGANIZATION", O.DBType("TEXT"))
  def email = column[String]("EMAIL", O.DBType("TEXT"))
  
  def pk = primaryKey("AUTHORS_PK", paperid ~ position)
  def paper = foreignKey("AUTHORS_PAPERID_FK", paperid, Papers)(_.id)
  def * = paperid ~ position ~ firstname ~ lastname ~ organization ~ email <> (Author, Author.unapply _)

  def all = DB.withSession(implicit session =>
    Query(Authors).list )
  
  def of(paper: Paper) = DB.withSession(implicit session =>
    Query(Authors).filter(_.paperid is paper.id).list )
  
  def insertAll(authors: List[Author]) = DB.withSession(implicit session =>
    authors.foreach(a => Authors.insert(a)) )
  
  def deleteFor(paper: Paper) = DB.withSession(implicit session =>
    Authors.filter(_.paperid is paper.id).delete )
}
