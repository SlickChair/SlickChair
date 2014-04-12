import org.joda.time.DateTime

package object models {
  type MetaData[M <: Model[M]] = (Id[M], DateTime, String)
  def ignoredId[M <: Model[M]]: Id[M] = Id[M](-1)
  // def metaData[M <: Model[M]](user: String, id: Id[M]): MetaData[M] = (id, DateTime.now, user)
  // def metaData[M <: Model[M]](user: String): MetaData[M] = metaData(user, ignoredId[M])
}
