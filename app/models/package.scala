import play.api.db.slick.Config.driver.simple._
import org.joda.time.DateTime
import controllers.Utils.SlickRequest
import java.util.UUID

package object models {
  type MetaData[M] = (Id[M], DateTime, String)
  type IdType = UUID
  def newId[M](): Id[M] = Id[M](UUID.randomUUID())
  def noMetadata[M]: MetaData[M] = (newId[M](), null, null)
  
  implicit def databaseSession(implicit database: Database): Session = database.session
}
