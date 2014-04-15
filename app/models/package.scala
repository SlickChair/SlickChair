import org.joda.time.DateTime
import java.util.UUID

package object models {
  type MetaData[M <: Model[M]] = (Id[M], DateTime, String)
  type IdType = UUID
  def newId[M <: Model[M]](): Id[M] = Id[M](UUID.randomUUID())
}
