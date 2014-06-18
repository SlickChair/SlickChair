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
          // Person("Heather", "Miller", "EPFL", "heather.miller@epfl.ch"),
          // Person("Philipp", "Haller", "EPFL", "philipp.haller@typesafe.com"),
          // Person("Viktor", "Kuncak", "EPFL", "vkuncak@gmail.com"),
          // ("Piskac", "Ruzica", "EPFL", "ruzica.piskac@yale.edu"),
          Person("Olivier", "Blanvillain", "EPFL", "olivierblanvillain@gmail.com")
        )
        
        connection insert chairs
        connection insert chairs.map(p => PersonRole(p.id, Chair)) 
        
        connection insert Workflow.setup
        
        // Demo authors
        val testPerson1 = Person(
          "SpongeBob",
          "SquarePants",
          "The Sea",
          "SpongeBobSquarePantsFakeEmail")
        val testPerson2 = Person(
          "Squidward",
          "Tentacles",
          "The Sea",
          "SquidwardTentaclesFakeEmail")
        connection insert List(testPerson1, testPerson2)

        // Demo papers
        val src = Source.fromFile("test/sigplanconf-template.pdf", "ISO8859-1").map(_.toByte).toArray
        List(
          ("Naughty Nautical Neighbors", Student_paper),
          ("Boating School", Student_paper),
          ("Pizza Delivery", Student_paper),
          ("Home Sweet Pineapple", Student_paper),
          ("Mermaid Man and Barnacle Boy", Student_paper)
        ) foreach { case (title, format) =>
          val file = File("sigplanconf.pdf", src.length, src)
          val paper = Paper(title, format, "keywords", "abstract", 2, Some(file.id), false)
          val paperIndex = PaperIndex(paper.id)
          connection insert file
          connection insert paper
          connection insert paperIndex
          connection insert List(PaperAuthor(paper.id, testPerson1.id, 0), PaperAuthor(paper.id, testPerson2.id, 1))
        }
        
        ()
      }
    }
  }
}

