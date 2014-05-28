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
      if(connection.database().topics.list.isEmpty) {
        
        val chairs = List(
          Person("Viktor", "Kuncak", "EPFL", "viktor.kuncak@epfl.ch"),
          Person("Viktor", "Kuncak", "EPFL", "vkuncak@gmail.com"),
          Person("Olivier", "Blanvillain", "EPFL", "olivierblanvillain@gmail.com")
        )
        
        val programCommitteeMembers = List(
          Person("Tihomir", "Gvero", "EPFL", "tihomir.gvero@epfl.ch"),
          Person("Etienne", "Kneuss", "EPFL", "etienne.kneuss@epfl.ch"),
          Person("Eva", "Darulova", "EPFL", "eva.darulova@epfl.ch"),
          Person("Regis", "Blanc", "EPFL", "regis.blanc@epfl.ch"),
          Person("Mikael", "Mayer", "EPFL", "mikael.mayer@epfl.ch"),
          Person("Ravi", "Kandhadai", "EPFL", "ravi.kandhadai@epfl.ch"),
          Person("Ivan", "Kuraj", "EPFL", "ivan.kuraj@epfl.ch"),
          Person("Andrew", "Reynolds", "EPFL", "andrew.reynolds@epfl.ch")
        )
        
        connection insert chairs
        connection insert chairs.map(p => PersonRole(p.id, Chair)) 
        connection insert programCommitteeMembers
        connection insert programCommitteeMembers.map(p => PersonRole(p.id, Reviewer))
        
        connection insert List(
          Topic("Language design and implementation"),
          Topic("Library design and implementation patterns for extending Scala"),
          Topic("Formal techniques for Scala-like programs"),
          Topic("Concurrent and distributed programming"),
          Topic("Safety and reliability"),
          Topic("Tools"),
          Topic("Case studies, experience reports, and pearls")
        )
        
        // Some demo papers
        val src = Source.fromFile("test/sigplanconf-template.pdf", "ISO8859-1").map(_.toByte).toArray
        connection insert (List(
          // Régis Blanc (epfl), etienne kneuss (epfl), viktor kuncak (epfl) and philippe suter (epfl)
          ("Verification by Translation to Recursive Functions ", Full_paper),
          // régis blanc (epfl)
          ("CafeSat: A Modern SAT Solver for Scala ", Tool_demo),
          // eugene burmako (epfl)
          ("Scala Macros: Let Our Powers Combine! ", Full_paper),
          // sébastien doeraene (epfl) and peter van roy (université catholique de louvain
          ("A New Concurrency Model for Scala Based on a Declarative Dataflow Core ", Full_paper),
          // paolo g. giarrusso (university of marburg)
          ("Open GADTs and Declaration-site Variance: A Problem Statement ", Short_paper),
          // christoph höger (tu berlin) and martin zuber (tu berlin)
          ("Towards a Tight Integration of a Functional Web Client Language into Scala ", Short_paper),
          // daniel kröni (fhnw) and raphael schweizer (fhnw)
          ("Parsing Graphs – Applying Parser Combinators to Graph Traversals ", Short_paper),
          // hubert plociniczak (epfl)
          ("Scalad: An Interactive Type-Level Debugger ", Tool_demo),
          // lukas stadler (johannes kepler university), gilles duboscq (johannes kepler university), hanspeter mössenböck (johannes kepler university), thomas wuerthinger (oracle labs) and doug simon (oracle labs)
          ("An Experimental Study of the Influence of Dynamic Compiler Optimizations on Scala Performance", Full_paper),
          // nicolas stucki (epfl) and vlad ureche (epfl)
          ("Bridging Islands of Specialized Code using Macros and Reified Types ", Short_paper),
          // sandro stucki (epfl), nada amin (epfl), manohar jonnalagedda (epfl) and tiark rompf (epfl, oracle labs)
          ("What are the Odds? – Probabilistic Programming in Scala ", Full_paper),
          // andré van delft
          ("Dataflow Constructs for a Language Extension Based on the Algebra of Communicating Processes", Full_paper)
        ) flatMap { case (title, format) =>
          val file = File("sigplanconf.pdf", src.length, src)
          val paper = Paper(title, format, "keywords", "abstract", 0, Some(file.id))
          val paperIndex = PaperIndex(paper.id)
          List(file, paper, paperIndex)
        })
        
        // Fake users with email login, passwords = 1234567890
        connection insert List(
          Person("Foo", "Bar", "Org", "chair"),
          Person("Foo", "Bar", "Org", "pcmember"))

        securesocial.core.UserService.save(User("chair", "userpass", "chair", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$i2jZu3F6rty/a0vj8Jbeb.BnZNW7dXutAM8wSXLIdIolJETt8YdWe"), None).toIdentity)
        securesocial.core.UserService.save(User("pcmember", "userpass", "pcmember", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$i2jZu3F6rty/a0vj8Jbeb.BnZNW7dXutAM8wSXLIdIolJETt8YdWe"), None).toIdentity)
        ()
      }
    }
  }
}
