package models.utils

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

// Email templates
case class Template (
  id: Int,
  name: String,
  subject: String,
  body: String
)
case class NewTemplate (name: String, subject: String, body: String)

object Templates extends Table[Template]("TEMPLATES") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME", O.DBType("TEXT"))
  def subject = column[String]("SUBJECT", O.DBType("TEXT"))
  def body = column[String]("BODY", O.DBType("TEXT"))
  
  def * =  id ~ name ~ subject ~ body <> (Template, Template.unapply _)
  def autoInc = name ~ subject ~ body <> (NewTemplate, NewTemplate.unapply _) returning id

  def all = DB.withSession(implicit session =>
    Query(Templates).list )

  def ins(newTemplate: NewTemplate) = DB.withSession(implicit session =>
    Templates.autoInc.insert(newTemplate) )

  // def delete(id: Int) = DB.withSession(implicit session =>
  //   Templates.filter(_.id is id).delete )
}