package models

// From: https://github.com/nafg/slick-additions
import scala.slick.lifted.{ BaseTypeMapper, MappedTypeMapper }
import scala.slick.jdbc.GetResult

trait Bitmasked {
  type Value
  def bitFor: Value => Int
  def forBit: Int => Value
  def values: Iterable[Value]
  def longToSet: Long => Set[Value] = bm => values.toSeq.filter(v => 0 != (bm & (1 << bitFor(v)))).toSet
  def setToLong: Set[Value] => Long = _.foldLeft(0L){ (bm, v) => bm + (1L << bitFor(v)) }

  implicit lazy val enumTypeMapper: BaseTypeMapper[Value] =
    MappedTypeMapper.base[Value, Int](bitFor, forBit)
  implicit lazy val enumSetTypeMapper: BaseTypeMapper[Set[Value]] =
   MappedTypeMapper.base[Set[Value], Long](setToLong, longToSet)

  implicit lazy val getResult: GetResult[Value] = GetResult(r => forBit(r.nextInt))
  implicit lazy val getSetResult: GetResult[Set[Value]] = GetResult(r => longToSet(r.nextLong))
}

// Mix this in to a subclass of Enumeration to get an implicit BaseTypeMapper and GetResult for V and Set[V].
trait BitmaskedEnumeration extends Bitmasked { this: Enumeration =>
  def bitFor = _.id
  def forBit = apply(_)
}