import play.api._
import org.joda.time.DateTime
import play.api.Logger
import models.entities._
import models.relations._
import models.secureSocial._
import models.utils._

/**
  * Populates the database with fake data for testing. Global.onStart() is
  * called when the application starts.
  */
object Global extends GlobalSettings {
  override def onStart(app: Application) {
    if(Topics.all.isEmpty) {
      List(
        NewTopic("Language design and implementation", "Language extensions, optimization, and performance evaluation."),
        NewTopic("Library design and implementation patterns for extending Scala", "Embedded domain-specific languages, combining language features, generic and meta-programming."),
        NewTopic("Formal techniques for Scala-like programs", "Formalizations of the language, type system, and semantics, formalizing proposed language extensions and variants, dependent object types, type and effect systems."),
        NewTopic("Concurrent and distributed programming", "Libraries, frameworks, language extensions, programming paradigms: (Actors, STM, ...), performance evaluation, experimental results."),
        NewTopic("Safety and reliability", "Pluggable type systems, contracts, static analysis and verification, runtime monitoring.")
      ).foreach(Topics.ins)
      
      List(
        NewPaper("1@1", DateTime.now, DateTime.now, None, "Paper 1", PaperType.Full_Paper, "key words 1", "abstrct 1", None),
        NewPaper("2@2", DateTime.now, DateTime.now, None, "Paper 2", PaperType.Full_Paper, "key words 2", "abstrct 2", None),
        NewPaper("3@3", DateTime.now, DateTime.now, None, "Paper 3", PaperType.Full_Paper, "key words 3", "abstrct 3", None)
      ).foreach(Papers.ins)
      
      List(
        PaperTopic(1, 1),
        PaperTopic(2, 2),
        PaperTopic(3, 3)
      ).foreach(PaperTopics.ins)
      
      Authors insertAll List(
        Author(1, 0, "first name 1", "last name 1", "org 1", "11@11"),
        Author(2, 0, "first name 2", "last name 2", "org 2", "22@22"),
        Author(3, 0, "first name 3", "last name 3", "org 3", "33@33"),
        Author(3, 1, "first name 31", "last name 31", "org 31", "33@331")
      )
      
      List(
        User("1@1", "userpass", "1@1", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None),
        User("2@2", "userpass", "2@2", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None),
        User("3@3", "userpass", "3@3", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None),
        User("4@4", "userpass", "4@4", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None)
      ).foreach(u => new SecureSocialUserService(null).save(u.toIdentity))
      
      List(
        NewMember("4@4", "4@4.com", DateTime.now, DateTime.now, MemberRole.Member, "membername", "memberlastname"),
        NewMember("olivierblanvillain@gmail.com", "olivier.blanvillain@epfl.ch", DateTime.now, DateTime.now, MemberRole.Chair, "membername", "memberlastname")
      ).foreach(Members.ins)
      
      List(
        Review(1, 1, Some(DateTime.now), Some(DateTime.now), ReviewConfidence.Low, ReviewEvaluation.Neutral, "thisismyreview!")
      ).foreach(Reviews.ins)
      
      List(
        NewComment(1, 1, DateTime.now, "I had to comment on this."),
        NewComment(1, 1, DateTime.now, "Twice.")
      ).foreach(Comments.ins)
      
      List(
        NewTemplate("Msg", "A message to @fullname", "Dear @firstname, \nThis message is about..."),
        NewTemplate("Warn", "A warrning to @fullname", "Dear @firstname, \nThis writting is about...")
      ).foreach(Templates.ins)
      
      List(
        MyToken(java.util.UUID.randomUUID().toString, "1@1", DateTime.now, DateTime.now.plusDays(7), false, true),
        MyToken(java.util.UUID.randomUUID().toString, "2@2", DateTime.now, DateTime.now.plusDays(7), false, true),
        MyToken(java.util.UUID.randomUUID().toString, "3@3", DateTime.now, DateTime.now.plusDays(7), false, true)
      ).foreach(SecureSocialTokens.ins)
      
      List(MemberTopic(2, 2)).foreach(MemberTopics.ins)
      List(MemberBid(2, 2, Bid.High)).foreach(MemberBids.ins)
    }
  }
}
