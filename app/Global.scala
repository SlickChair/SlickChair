import io.Source

import models._
import Role._
import PaperType._
import play.api.{Application, GlobalSettings}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {
  override def onStart(app: Application): Unit = {
    DB withSession { implicit s: Session =>
      val connection = Connection(s)
      if(connection.database().persons.list.isEmpty) {
        
        val chairs = List(
          Person("Ruzica", "Piskac", "EPFL", "ruzica.piskac@yale.edu"),
          Person("Viktor", "Kuncak", "EPFL", "viktor.kuncak@epfl.ch"),
          Person("Viktor", "Kuncak", "EPFL", "vkuncak@gmail.com"),
          Person("Olivier", "Blanvillain", "EPFL", "olivierblanvillain@gmail.com")
        )
        
        connection insert chairs
        connection insert chairs.map(p => PersonRole(p.id, Chair)) 
        
        // A demo paper
        val src = Source.fromFile("test/sigplanconf-template.pdf", "ISO8859-1").map(_.toByte).toArray
        connection insert {
          val title = "Test paper"
          val format = Student_paper
          val file = File("sigplanconf.pdf", src.length, src)
          val paper = Paper(title, format, "keywords", "abstract", 0, Some(file.id))
          val paperIndex = PaperIndex(paper.id)
          List(file, paper, paperIndex)
        }
        
        ()
      }
    }
  }
}
