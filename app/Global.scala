import org.joda.time.DateTime

import models._
import PersonRole._
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
      val connection = Connection(s)
      if(connection.database().topics.list.isEmpty) {
        
        val chairs = List(
          Person("Olivier", "Blanvillain", "EPFL", "olivierblanvillain@gmail.com")
        )
        
        val programCommitteeMembers = List(
          Person("Foo", "Bar", "Org", "pcmember")
        )
        
        // List(Person(Olivier,Blanvillain,EPFL,olivierblanvillain@gmail.com,(Id(20484e20-40e6-4504-bec7-db9464485e2c),null,null)))
        
        // List(Role(Id(6f6c6976-5a91-1e95-939e-91898b3ccea0),Chair,(Id(6caf1565-27e2-42cf-b5ca-85f5e324f36f),null,null)))
        
        connection insert chairs
        connection insert chairs.map(p => Role(p.id, Chair)) 
        connection insert programCommitteeMembers
        connection insert programCommitteeMembers.map(p => Role(p.id, Reviewer))
        
        connection insert List(
          Topic("Language design and implementation"),
          Topic("Library design and implementation patterns for extending Scala"),
          Topic("Formal techniques for Scala-like programs"),
          Topic("Concurrent and distributed programming"),
          Topic("Safety and reliability"),
          Topic("Tools"),
          Topic("Case studies, experience reports, and pearls")
        )
        
        // Some demo papers.
        val src = Source.fromFile("test/sigplanconf-template.pdf", "ISO8859-1").map(_.toByte).toArray
        connection insert (List(
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
        ) flatMap { case (title, format) =>
          val file = File("sigplanconf.pdf", src.length, src)
          val paper = Paper(title, format, "keywords", "abstract", 0, Some(file.id))
          val paperIndex = PaperIndex(paper.id)
          List(file, paper, paperIndex)
        })
        
        // Passwords = 1234567890
        securesocial.core.UserService.save(User("pcmember", "userpass", "pcmember", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$i2jZu3F6rty/a0vj8Jbeb.BnZNW7dXutAM8wSXLIdIolJETt8YdWe"), None).toIdentity)
        
        ()
      }
    }
  }
}
