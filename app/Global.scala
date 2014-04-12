import org.joda.time.DateTime

import models._
import play.api.{ Application, GlobalSettings }
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import play.api.db.slick.DB

/** Populates the database with fake data for testing. Global.onStart() is
  * called when the application starts.
  */
object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    DB withSession { implicit s: Session =>
      if(Topics.all.isEmpty) {
        // Passwords = 1234567890
        securesocial.core.UserService.save(User("4@4", "userpass", "4@4", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$i2jZu3F6rty/a0vj8Jbeb.BnZNW7dXutAM8wSXLIdIolJETt8YdWe"), None).toIdentity)
              
        val now: DateTime = DateTime.now
        List(
          Topic((ignoredId, now, "demo"), "Language design and implementation", "Language extensions, optimization, and performance evaluation."),
          Topic((ignoredId, now, "demo"), "Library design and implementation patterns for extending Scala", "Embedded domain-specific languages, combining language features, generic and meta-programming."),
          Topic((ignoredId, now, "demo"), "Formal techniques for Scala-like programs", "Formalizations of the language, type system, and semantics, formalizing proposed language extensions and variants, dependent object types, type and effect systems."),
          Topic((ignoredId, now, "demo"), "Concurrent and distributed programming", "Libraries, frameworks, language extensions, programming paradigms: (Actors, STM, ...), performance evaluation, experimental results."),
          Topic((ignoredId, now, "demo"), "Safety and reliability", "Pluggable type systems, contracts, static analysis and verification, runtime monitoring.")
        ).foreach(Topics.insert)
      }
      ()
        
      //   List(
      //     NewPaper("1@1", "fn", "ln", DateTime.now, DateTime.now, None, "Paper 1", PaperType.Full_Paper, "key words 1", "abstrct 1", None),
      //     NewPaper("2@2", "fn", "ln", DateTime.now, DateTime.now, None, "Paper 2", PaperType.Full_Paper, "key words 2", "abstrct 2", None),
      //     NewPaper("3@3", "fn", "ln", DateTime.now, DateTime.now, None, "Paper 3", PaperType.Full_Paper, "key words 3", "abstrct 3", None)
      //   ).foreach(Papers.insert)
        
      //   List(
      //     PaperTopic(1, 1),
      //     PaperTopic(2, 2),
      //     PaperTopic(3, 3)
      //   ).foreach(PaperTopics.insert)
        
      //   Authors insertAll List(
      //     Author(1, 0, "first name 1", "last name 1", "org 1", "11@11"),
      //     Author(2, 0, "first name 2", "last name 2", "org 2", "22@22"),
      //     Author(3, 0, "first name 3", "last name 3", "org 3", "33@33"),
      //     Author(3, 1, "first name 31", "last name 31", "org 31", "33@331")
      //   )
        
      //   List(
      //     NewMember("4@4", "4@4.com", DateTime.now, DateTime.now, PersonRole.Member, "membername", "memberlastname"),
      //     NewMember("olivierblanvillain@gmail.com", "olivier.blanvillain@epfl.ch", DateTime.now, DateTime.now, PersonRole.Chair, "membername", "memberlastname")
      //   ).foreach(Persons.insert)
        
      //   List(
      //     Review(1, 1, Some(DateTime.now), Some(DateTime.now), ReviewConfidence.Low, ReviewEvaluation.Neutral, "thisismyreview!")
      //   ).foreach(Reviews.insert)
        
      //   List(
      //     NewComment(1, 1, DateTime.now, "I had to comment on this."),
      //     NewComment(1, 1, DateTime.now, "Twice.")
      //   ).foreach(Comments.insert)
        
      //   List(
      //     NewEmailTemplate("Msg", "A message to @fullname", "Dear @firstname, \nThis message is about..."),
      //     NewEmailTemplate("Warn", "A warrning to @fullname", "Dear @firstname, \nThis writting is about...")
      //   ).foreach(EmailTemplates.insert)
        
      //   List(
      //     MyToken(java.util.UUID.randomUUID().toString, "1@1", DateTime.now, DateTime.now.plusDays(7), false, true),
      //     MyToken(java.util.UUID.randomUUID().toString, "2@2", DateTime.now, DateTime.now.plusDays(7), false, true),
      //     MyToken(java.util.UUID.randomUUID().toString, "3@3", DateTime.now, DateTime.now.plusDays(7), false, true)
      //   ).foreach(LoginTokens.insert)
        
      //   // List(MemberTopic(2, 2)).foreach(MemberTopics.insert)
      //   // List(MemberBid(2, 2, Bid.High)).foreach(MemberBids.insert)
      // }
    }
  }
}
