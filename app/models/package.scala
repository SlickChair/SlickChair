import play.api.db.slick.Config.driver.simple._
import org.joda.time.DateTime
import controllers.Utils.SlickRequest
import java.util.UUID

package object models {
  type Metadata[M] = (Id[M], DateTime, String)
  type IdType = UUID
  def newMetadata[M]: Metadata[M] = withId(Id[M](UUID.randomUUID()))
  def withId[M](id: Id[M]): Metadata[M] = (id, null, null)
}
