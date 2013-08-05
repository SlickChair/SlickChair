package models

import slick.jdbc.GetResult
import slick.lifted.{BaseTypeMapper, MappedTypeMapper}

/**
  * The BitmaskedEnumeration trait can be mixed into a subclass of Enumeration
  * in to have is usable as a column type in a Slick Table. Source:
  * https://github.com/nafg/slick-additions
  */
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

trait BitmaskedEnumeration extends Bitmasked { this: Enumeration =>
  def bitFor = _.id
  def forBit = apply(_)
}
