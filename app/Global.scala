import org.joda.time.DateTime

import models._
import models.PaperType._
import play.api.{ Application, GlobalSettings }
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import play.api.db.slick.DB
import scala.io.Source

/** Populates the database with fake data for testing. Global.onStart() is
  * called when the application starts. */
object Global extends GlobalSettings {
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
        ) foreach (Topics ins Topic((newId(), now, "demo"), _))
        
        val src = Source.fromFile("test/sigplanconf-template.pdf", "ISO8859-1").map(_.toByte).toArray
        
        List(
          ("Verification by Translation to Recursive Functions ", Full_Paper),
          ("CafeSat: A Modern SAT Solver for Scala ", Tool_Demo),
          ("Scala Macros: Let Our Powers Combine! ", Full_Paper),
          ("A New Concurrency Model for Scala Based on a Declarative Dataflow Core ", Full_Paper),
          ("Open GADTs and Declaration-site Variance: A Problem Statement ", Short_Paper),
          ("Towards a Tight Integration of a Functional Web Client Language into Scala ", Short_Paper),
          ("Parsing Graphs – Applying Parser Combinators to Graph Traversals ", Short_Paper),
          ("Scalad: An Interactive Type-Level Debugger ", Tool_Demo),
          ("An Experimental Study of the Influence of Dynamic Compiler Optimizations on Scala Performance", Full_Paper),
          ("Bridging Islands of Specialized Code using Macros and Reified Types ", Short_Paper),
          ("What are the Odds? – Probabilistic Programming in Scala ", Full_Paper),
          ("Dataflow Constructs for a Language Extension Based on the Algebra of Communicating Processes", Full_Paper)
        ) foreach { case (title, format) =>
          val pdf = Files ins File((newId(), now, "demo"), "sigplanconf.pdf", src.length, src)
          Papers ins Paper((newId(), now, "demo"), title, format, "keywords", "abstract", 0, Some(pdf))
        }
        
        // Authors:
        // Régis Blanc (epfl), etienne kneuss (epfl), viktor kuncak (epfl) and philippe suter (epfl)
        // régis blanc (epfl)
        // eugene burmako (epfl)
        // sébastien doeraene (epfl) and peter van roy (université catholique de louvain
        // paolo g. giarrusso (university of marburg)
        // christoph höger (tu berlin) and martin zuber (tu berlin)
        // daniel kröni (fhnw) and raphael schweizer (fhnw)
        // hubert plociniczak (epfl)
        // lukas stadler (johannes kepler university), gilles duboscq (johannes kepler university), hanspeter mössenböck (johannes kepler university), thomas wuerthinger (oracle labs) and doug simon (oracle labs)
        // nicolas stucki (epfl) and vlad ureche (epfl)
        // sandro stucki (epfl), nada amin (epfl), manohar jonnalagedda (epfl) and tiark rompf (epfl, oracle labs)
        // andré van delft
      }
    }
  }
}
