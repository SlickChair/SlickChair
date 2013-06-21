package models.utils

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import java.sql.Date
import org.joda.time.DateTime

// Files
case class File(
  id: Int,
  name: String,
  size: Long,
  uploaded: DateTime,
  data: Array[Byte]
)
case class NewFile(name: String, size: Long, uploaded: DateTime, data: Array[Byte])

object Files extends Table[File]("FILES") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME", O.DBType("TEXT"))
  def size = column[Long]("SIZE")
  def uploaded = column[DateTime]("UPLOADED")
  def data = column[Array[Byte]]("DATA")
  
  def * =  id ~ name ~ size ~ uploaded ~ data <> (File, File.unapply _)
  def autoInc = name ~ size ~ uploaded ~ data <> (NewFile, NewFile.unapply _) returning id

  def all = DB.withSession(implicit session =>
    Query(Files).list )
  
  def ins(newFile: NewFile) = DB.withSession(implicit session =>
    Files.autoInc.insert(newFile) )
  
  def delete(id: Int) = DB.withSession(implicit session =>
    Files.filter(_.id is id).delete )
}

