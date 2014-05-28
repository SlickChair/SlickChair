package controllers

import models._
import Role.Chair
import Decision.Decision
import play.api.mvc.Controller
import Mappers.{enumFormMapping, idFormMapping}
import play.api.data.Mapping
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, boolean}
import play.api.data.Mapping
import BidValue.Maybe


case class AssignmentForm(assignments: List[Assignment])
case class DecisionForm(aecisions: List[PaperDecision])

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
  
  def decisionFormMapping: Mapping[PaperDecision] = mapping(
    "paperid" -> ignored(newMetadata[Paper]._1),
    "value" -> enumFormMapping(Decision),
    "metadata" -> ignored(newMetadata[PaperDecision])
  )(PaperDecision.apply _)(PaperDecision.unapply _)
  
  def decisionForm: Form[DecisionForm] = Form(
    mapping("decisions" -> list(decisionFormMapping))
    (DecisionForm.apply _)(DecisionForm.unapply _)
  )
  
  def assignmentList() = SlickAction(IsChair) { implicit r =>
    Ok(views.html.assignmentlist(Query(r.db).allPapers, Query(r.db).allPaperIndices, Query(r.db).allAssignments, Navbar(Chair)))
  }
  
  def assign(paperId: Id[Paper]) = SlickAction(IsChair) { implicit r =>
    val bids = Query(r.db) bidsOn paperId
    val assignments = Query(r.db) assignmentsOn paperId
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
    play.api.Logger.error(form.toString)
    Ok(views.html.assignment(
      paperId,
      Query(r.db).indexOf(paperId),
      sortedStaff,
      form,
      allBids,
      Query(r.db).allAssignments,
      Navbar(Chair)
    )(Submitting.summary(paperId)))
  }

  def doAssign(paperId: Id[Paper]) = SlickAction(IsChair) { implicit r =>
    assignmentForm.bindFromRequest.fold(
      errors => 
        Redirect(routes.Chairing.assign(paperId)),
      form => {
        val assignments = form.assignments map { _ copy (paperid=paperId) }
        r.connection.insert(assignments)
        Redirect(routes.Chairing.assign(paperId))
      }
    )
  }
  
  def decision = SlickAction(IsChair) { implicit r =>
    ???
  }
  def doDecision = SlickAction(IsChair) { implicit r =>
    ???
  }
  def decide = SlickAction(IsChair) { implicit r => 
    ???
  }
  def doDecide = SlickAction(IsChair) { implicit r => 
    ???
  }
}
