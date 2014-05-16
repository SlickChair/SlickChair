import org.joda.time.DateTime
import controllers.Utils.SlickRequest
import java.util.UUID

package object models {
  type MetaData[M] = (Id[M], DateTime, String)
  type IdType = UUID
  def newId[M](): Id[M] = Id[M](UUID.randomUUID())
  val noMetaDate = (null, null, null)
  // def newMetaData[M <: Model[M]]()(implicit r: SlickRequest[_]): MetaData[M] =
  //   (newId[M](), r.now, r.user.email)
}
