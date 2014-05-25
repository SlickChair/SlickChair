package controllers

import models._
import PersonRole.Chair
import play.api.mvc.Controller
import Mappers.idFormMapping
import play.api.data.Mapping
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, boolean}
import play.api.data.Mapping
import BidValue.Maybe

case class AssignmentForm(assignments: List[Assignment])

object Chairing extends Controller {
  def assignmentFormMapping: Mapping[Assignment] = mapping(
    "paperid" -> ignored(newMetadata[Paper]._1),
    "personid" -> idFormMapping[Person],
    "value" -> boolean,
    "metadata" -> ignored(newMetadata[Assignment])
  )(Assignment.apply _)(Assignment.unapply _)

  def assignmentForm: Form[AssignmentForm] = Form(
    mapping("assignments" -> list(assignmentFormMapping))
    (AssignmentForm.apply _)(AssignmentForm.unapply _)
  )
  
  def assignmentList() = SlickAction(IsChair) { implicit r =>
    Ok(views.html.assignmentlist(Query(r.db).allPapers, Query(r.db).allPaperIndices, Query(r.db).allAssignments, Navbar(Chair)))
  }
  
  def assign(paperId: Id[Paper]) = SlickAction(IsChair) { implicit r =>
    val bids = Query(r.db) bidsOn paperId
    val assignments = Query(r.db) assignmentOn paperId
    val sortedStaff = Query(r.db).allStaff
      .sortBy { s => bids find (_.personid == s.id) map (_.value.id) getOrElse Maybe.id }
      .sortBy { s => assignments exists (_.personid == s.id) }
      .reverse
    val allBids = sortedStaff map { p => 
      bids.find(_.personid == p.id) match {
        case None => Bid(paperId, p.id, Maybe)
        case Some(b) => b
      }
    }
    val allAssignments = sortedStaff map { s =>
      assignments.find(_.personid == s.id) match {
        case None => Assignment(paperId, s.id, false)
        case Some(a) => a
      }
    }
    val form = assignmentForm fill AssignmentForm(allAssignments)
    Ok(views.html.assignment(
      paperId,
      Query(r.db).indexOf(paperId),
      sortedStaff,
      form,
      allBids,
      Navbar(Chair)
    )(Submitting.summary(paperId)))
  }

  def doAssign(paperId: Id[Paper]) = TODO
  // SlickAction(IsChair) { implicit r =>
  //   Ok("doAssign")
  // }
}
