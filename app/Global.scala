import play.api._
import models.entities._
import org.joda.time.DateTime
import models.entities.PaperFormat._
import models.relations._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    if(Topics.all.isEmpty) {
      List(Topic(None, "Language design and implementation", "language extensions, optimization, and performance evaluation."),
      Topic(None, "Library design and implementation patterns for extending Scala", "embedded domain-specific languages, combining language features, generic and meta-programming."),
      Topic(None, "Formal techniques for Scala-like programs", "formalizations of the language, type system, and semantics, formalizing proposed language extensions and variants, dependent object types, type and effect systems."),
      Topic(None, "Concurrent and distributed programming", "libraries, frameworks, language extensions, programming paradigms: (Actors, STM, ...), performance evaluation, experimental results."),
      Topic(None, "Safety and reliability", "pluggable type systems, contracts, static analysis and verification, runtime monitoring."),
      Topic(None, "Tools", "development environments, debuggers, refactoring tools, testing frameworks."),
      Topic(None, "Case studies, experience reports, and pearls.", "empty")
      ).foreach(Topics.ins)
      
      val paperId = Papers.ins(Paper(None,"TODO_FROM_SECURESOCIAL@gmail.com",DateTime.now,DateTime.now,None,"Scala Macros: Let Our Powers Combine!",Standard,"Macro, Compilation","Compile-time metaprogramming has been proven immensely useful",None))
      Authors.ins(Author(paperId,0,"Eugene","Burmako","EPFL","eugene.burmako@epﬂ"))
      List(1, 2, 6).foreach(topicId => PaperTopics.ins(PaperTopic(paperId, topicId)))
    }
  }
}
