package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

case class Task(id: Long, label: String)

object Tasks extends Table[Task]("TASKS") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def label = column[String]("label", O.NotNull)
  def * = id ~ label <> (Task.apply _, Task.unapply _)
  def ins = label
  
  def insert(label: String) = {
    DB.withSession{ implicit session =>
      Tasks.ins.insert(label)
    }
  }
  
  def delete(id: Long) = {
    DB.withSession { implicit session =>
      Tasks.where(_.id === id).delete
    }
  }
  
  def all = {
    DB.withSession { implicit session =>
      Query(Tasks).list
    }
  }
}
