package models.relations

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models._
import models.entities._

object Affinity extends Enumeration with BitmaskedEnumeration {
  type Affinity = Value
  val Conflict, Low, Medium, High = Value
}
import Affinity._

// Conflicts between submissions and reviewers
case class ReviewerAffinitie(
  paperid: Int,
  reviewerid: Int,
  affinity: Affinity
)

object ReviewerAffinities extends Table[ReviewerAffinitie]("REVIEWER_AFFINITIES"){
  def paperid = column[Int]("paperid")
  def reviewerid = column[Int]("reviewerid")
  def affinity = column[Affinity]("affinity")
  
  def pk = primaryKey("revieweraffinities_pk", (paperid, reviewerid))
  def reviewer = foreignKey("reviewerid_fk", reviewerid, Reviewers)(_.id)
  def paper = foreignKey("paperid_fk", paperid, Papers)(_.id)
  
  def * = paperid ~ reviewerid ~ affinity <> (ReviewerAffinitie.apply _, ReviewerAffinitie.unapply _)
}