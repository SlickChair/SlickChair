import java.util.UUID
import org.joda.time.DateTime
import models.Id

package object models {
  type Metadata[M] = (Id[M], DateTime, String)
  type IdType = UUID
  def newMetadata[M]: Metadata[M] = withId(Id[M](UUID.randomUUID()))
  def withId[M](id: Id[M]): Metadata[M] = (id, null, null)
}
