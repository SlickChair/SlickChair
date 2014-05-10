import org.joda.time.DateTime
import java.util.UUID
import controllers.Utils.SlickRequest

package object models {
  type MetaData[M <: Model[M]] = (Id[M], DateTime, String)
  type IdType = UUID
  def newId[M <: Model[M]](): Id[M] = Id[M](UUID.randomUUID())
  def newMetaData[M <: Model[M]]()(implicit r: SlickRequest[_]): MetaData[M] =
    (newId[M](), r.now, r.user.email)
}
