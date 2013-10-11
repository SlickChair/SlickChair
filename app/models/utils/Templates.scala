package models.utils

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

/** This file holds all the code related to the storage of Email in the
  * database.
  * @see models.Members.scala for a similar implementation with ScalaDoc.
  */

case class EmailTemplate (
  id: Int,
  name: String,
  subject: String,
  body: String
)
case class NewEmailTemplate (name: String, subject: String, body: String)

object EmailTemplates extends Table[EmailTemplate]("EMAIL_TEMPLATES") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME", O.DBType("TEXT"))
  def subject = column[String]("SUBJECT", O.DBType("TEXT"))
  def body = column[String]("BODY", O.DBType("TEXT"))
  
  def * =  id ~ name ~ subject ~ body <> (EmailTemplate, EmailTemplate.unapply _)
  def autoInc = name ~ subject ~ body <> (NewEmailTemplate, NewEmailTemplate.unapply _) returning id

  def all = DB.withSession{implicit session:Session =>
    Query(EmailTemplates).list }

  def ins(newEmailTemplate: NewEmailTemplate) = DB.withSession{implicit session:Session =>
    EmailTemplates.autoInc.insert(newEmailTemplate) }

  // def delete(id: Int) = DB.withSession({implicit session:Session =>
  //   MailTemplates.filter(_.id is id).delete)}
}
