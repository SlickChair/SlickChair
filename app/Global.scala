import org.joda.time.DateTime

import models._
import play.api._
import play.api.mvc.WithFilters
import models.PaperType._
import play.api.{ Application, GlobalSettings }
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import play.api.db.slick.DB
import play.filters.gzip.GzipFilter
import scala.io.Source

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {
/** Populates the database with fake data for testing. Global.onStart() is
  * called when the application starts. */
  override def onStart(app: Application): Unit = {
    DB withSession { implicit s: Session =>
      if(Topics.all.isEmpty) {
        // Passwords = 1234567890
        securesocial.core.UserService.save(User("4@4", "userpass", "4@4", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$i2jZu3F6rty/a0vj8Jbeb.BnZNW7dXutAM8wSXLIdIolJETt8YdWe"), None).toIdentity)
        
        val now: DateTime = DateTime.now
        List(
          "Language design and implementation",
          "Library design and implementation patterns for extending Scala",
          "Formal techniques for Scala-like programs",
          "Concurrent and distributed programming",
          "Safety and reliability",
          "Tools",
          "Case studies, experience reports, and pearls"
        ) foreach (Topics ins Topic(_))
        
        // Some demo papers.
        val src = Source.fromFile("test/sigplanconf-template.pdf", "ISO8859-1").map(_.toByte).toArray
        List(
          // Régis Blanc (epfl), etienne kneuss (epfl), viktor kuncak (epfl) and philippe suter (epfl)
          ("Verification by Translation to Recursive Functions ", Full_Paper),
          // régis blanc (epfl)
          ("CafeSat: A Modern SAT Solver for Scala ", Tool_Demo),
          // eugene burmako (epfl)
          ("Scala Macros: Let Our Powers Combine! ", Full_Paper),
          // sébastien doeraene (epfl) and peter van roy (université catholique de louvain
          ("A New Concurrency Model for Scala Based on a Declarative Dataflow Core ", Full_Paper),
          // paolo g. giarrusso (university of marburg)
          ("Open GADTs and Declaration-site Variance: A Problem Statement ", Short_Paper),
          // christoph höger (tu berlin) and martin zuber (tu berlin)
          ("Towards a Tight Integration of a Functional Web Client Language into Scala ", Short_Paper),
          // daniel kröni (fhnw) and raphael schweizer (fhnw)
          ("Parsing Graphs – Applying Parser Combinators to Graph Traversals ", Short_Paper),
          // hubert plociniczak (epfl)
          ("Scalad: An Interactive Type-Level Debugger ", Tool_Demo),
          // lukas stadler (johannes kepler university), gilles duboscq (johannes kepler university), hanspeter mössenböck (johannes kepler university), thomas wuerthinger (oracle labs) and doug simon (oracle labs)
          ("An Experimental Study of the Influence of Dynamic Compiler Optimizations on Scala Performance", Full_Paper),
          // nicolas stucki (epfl) and vlad ureche (epfl)
          ("Bridging Islands of Specialized Code using Macros and Reified Types ", Short_Paper),
          // sandro stucki (epfl), nada amin (epfl), manohar jonnalagedda (epfl) and tiark rompf (epfl, oracle labs)
          ("What are the Odds? – Probabilistic Programming in Scala ", Full_Paper),
          // andré van delft
          ("Dataflow Constructs for a Language Extension Based on the Algebra of Communicating Processes", Full_Paper)
        ) foreach { case (title, format) =>
          val pdf = Files ins File("sigplanconf.pdf", src.length, src)
          Papers ins Paper(title, format, "keywords", "abstract", 0, Some(pdf))
        }
        
      }
    }
  }
}
