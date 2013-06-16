import play.api._
import org.joda.time.DateTime
// import models.entities.PaperFormat._
import models.entities._
import models.relations._
import models.secureSocial._

/**
 * Populates the database with fake data for testing. Global.onStart() is called when the application starts.
 */
object Global extends GlobalSettings {
  override def onStart(app: Application) {
    if(Topics.all.isEmpty) {
      List(
        Topic(None, "Topic 1", "Description 1"),
        Topic(None, "Topic 2", "Description 2"),
        Topic(None, "Topic 3", "Description 3"),
        Topic(None, "Topic 4", "Description 4"),
        Topic(None, "Topic 5", "Description 5")
      ).foreach(Topics.ins)
      
      List(
        Paper(None, "1@1", DateTime.now, DateTime.now, None, "Paper 1", PaperFormat.Standard, "key words 1", "abstrct 1", None),
        Paper(None, "2@2", DateTime.now, DateTime.now, None, "Paper 2", PaperFormat.Standard, "key words 2", "abstrct 2", None),
        Paper(None, "3@3", DateTime.now, DateTime.now, None, "Paper 3", PaperFormat.Standard, "key words 3", "abstrct 3", None),
        Paper(None, "4@4", DateTime.now, DateTime.now, None, "Paper 4", PaperFormat.Standard, "key words 4", "abstrct 4", None)
      ).foreach(Papers.ins)
      
      List(
        PaperTopic(1, 1),
        PaperTopic(2, 2),
        PaperTopic(3, 3),
        PaperTopic(4, 4)
      ).foreach(PaperTopics.ins)
      
      Authors createAll List(
        Author(1, 0, "first name 1", "last name 1", "org 1", "11@11"),
        Author(2, 0, "first name 2", "last name 2", "org 2", "22@22"),
        Author(3, 0, "first name 3", "last name 3", "org 3", "33@33"),
        Author(4, 0, "first name 4", "last name 4", "org 4", "44@44"),
        Author(4, 1, "first name 41", "last name 41", "org 41", "44@441")
      )
      
      List(
        User("1@1", "userpass", "1@1", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None),
        User("2@2", "userpass", "2@2", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None),
        User("3@3", "userpass", "3@3", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None),
        User("4@4", "userpass", "4@4", "firstname", "lastname", "userPassword", Some("bcrypt"), Some("$2a$10$lR2Qcz7OolHLXGDgbKurF.n6E9yTFHVHHutrfMeKls.X5y/WbzUWq"), None)
      ).foreach(u => new SecureSocialUserService(null).save(u.toIdentity))
    }
  }
}
