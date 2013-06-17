package models.entities

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import _root_.java.sql.Date
import org.joda.time.DateTime

// Files
case class File(
  id: Option[Int],
  name: String,
  size: Long,
  uploaded: DateTime,
  data: Array[Byte]
)

object Files extends Table[File]("Files") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME", O.DBType("TEXT"))
  def size = column[Long]("SIZE")
  def uploaded = column[DateTime]("UPLOADED")
  def data = column[Array[Byte]]("DATA")
  
  def * = id.? ~ name ~ size ~ uploaded ~ data <> (File, File.unapply _)
  def autoInc = * returning id

  def all = DB.withSession(implicit session =>
    Query(Files).list )
  
  def ins(file: File) = DB.withSession(implicit session =>
    Files.autoInc.insert(file) )
  
  def delete(id: Int) = DB.withSession(implicit session =>
    Files.filter(_.id is id).mutate(_.delete) )
}

