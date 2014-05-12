import org.joda.time.DateTime
import controllers.Utils.SlickRequest

package object models {
  type MetaData[M <: Model[M]] = (Id[M], DateTime, String)
  type IdType = Long
  def newId[M <: Model[M]](): Id[M] = Id[M](-1)
  def newMetaData[M <: Model[M]]()(implicit r: SlickRequest[_]): MetaData[M] =
    (newId[M](), r.now, r.user.email)
}
