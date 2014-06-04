package controllers

import models._
import Role.Chair
import Evaluation.Evaluation
import Decision.{Decision, Undecided}
import play.api.mvc.Controller
import Mappers.{enumFormMapping, idFormMapping}
import play.api.data.Mapping
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, boolean}
import play.api.data.Mapping
import BidValue.Maybe

case class AssignmentForm(assignments: List[Assignment])
case class DecisionForm(decisions: List[PaperDecision])
case class RolesForm(roles: List[PersonRole])

object Chairing extends Controller {
  def assignmentFormMapping: Mapping[Assignment] = mapping(
    "paperId" -> ignored(newMetadata[Paper]._1),
    "personId" -> idFormMapping[Person],
    "value" -> boolean,
    "metadata" -> ignored(newMetadata[Assignment])
  )(Assignment.apply _)(Assignment.unapply _)

  def assignmentForm: Form[AssignmentForm] = Form(
    mapping("assignments" -> list(assignmentFormMapping))
    (AssignmentForm.apply _)(AssignmentForm.unapply _)
  )
  
  def decisionFormMapping: Mapping[PaperDecision] = mapping(
    "paperId" -> idFormMapping[Paper],
    "value" -> enumFormMapping(Decision),
    "metadata" -> ignored(newMetadata[PaperDecision])
  )(PaperDecision.apply _)(PaperDecision.unapply _)
  
  def decisionForm: Form[DecisionForm] = Form(
    mapping("decisions" -> list(decisionFormMapping))
    (DecisionForm.apply _)(DecisionForm.unapply _)
  )
  
  def rolesFormMapping: Mapping[PersonRole] = mapping(
    "personId" -> idFormMapping[Person],
    "value" -> enumFormMapping(Role),
    "metadata" -> ignored(newMetadata[PersonRole])
  )(PersonRole.apply _)(PersonRole.unapply _)
  
  def rolesForm: Form[RolesForm] = Form(
    mapping("roles" -> list(rolesFormMapping))
    (RolesForm.apply _)(RolesForm.unapply _)
  )
  
  def assignmentList() = SlickAction(IsChair, _.alwaysEnabled) { implicit r =>
    Ok(views.html.assignmentlist(Query(r.db).allPapers, Query(r.db).allPaperIndices, Query(r.db).allAssignments, Navbar(Chair)))
  }
  
  def assign(paperId: Id[Paper]) = SlickAction(IsChair, _.chairAssignment) {
    implicit r =>
    val bids = Query(r.db) bidsOn paperId
    val assignments = Query(r.db) assignmentsOn paperId
    val sortedStaff = Query(r.db).allStaff
      .sortBy { s => bids find (_.personId == s.id) map (_.value.id) getOrElse Maybe.id }
      .sortBy { s => assignments exists (_.personId == s.id) }
      .reverse
    val allBids = sortedStaff map { p => 
      bids.find(_.personId == p.id) match {
        case None => Bid(paperId, p.id, Maybe)
        case Some(b) => b
      }
    }
    val allAssignments = sortedStaff map { s =>
      assignments.find(_.personId == s.id) match {
        case None => Assignment(paperId, s.id, false)
        case Some(a) => a
      }
    }
    val form = assignmentForm fill AssignmentForm(allAssignments)
    Ok(views.html.assignment(paperId, Query(r.db).indexOf(paperId), sortedStaff, form, allBids, Query(r.db).allAssignments, Navbar(Chair)
    )(Submitting.summaryImpl(paperId)))
  }

  def doAssign(paperId: Id[Paper]) = SlickAction(IsChair, _.chairAssignment) {
    implicit r =>
    assignmentForm.bindFromRequest.fold(
      errors => 
        Redirect(routes.Chairing.assign(paperId)),
      form => {
        val assignments = form.assignments map { _ copy (paperId=paperId) }
        r.connection insert assignments
        Redirect(routes.Chairing.assign(paperId))
      }
    )
  }
  
  def decision = SlickAction(IsChair, _.chairDecision) { implicit r =>
    val decisions: List[PaperDecision] = Query(r.db).allPaperDecisions
    val papers: List[Paper] = Query(r.db).allPapers
    val allPaperDecisions: List[PaperDecision] = papers map { p =>
      decisions.find(_.paperId == p.id) match {
        case None => PaperDecision(p.id, Undecided)
        case Some(d) => d
      }
    }
    val form = decisionForm fill DecisionForm(allPaperDecisions)

    val indices: List[PaperIndex] = Query(r.db).allPaperIndices
    val reviews: List[Review] = Query(r.db).allReviews
    val paperIndexEvaluations: Id[Paper] => Option[(Paper, Int, List[Evaluation])] = paperId => 
      for(
        paper <- papers.find(_.id == paperId);
        index <- indices.zipWithIndex.find(_._1.paperId == paperId).map(_._2)
      ) yield (paper, index, reviews.filter(_.paperId == paperId).map(_.evaluation))
      
    Ok(views.html.decision(form, paperIndexEvaluations, Navbar(Chair)))
  }
  
  def doDecision = SlickAction(IsChair, _.chairDecision) { implicit r =>
    decisionForm.bindFromRequest.fold(_ => (), form => r.connection insert form.decisions)
    Redirect(routes.Chairing.decision)
  }
  
  def submissions = SlickAction(IsChair, _.alwaysEnabled) { implicit r => 
    Reviewing.submissionsImpl(routes.Chairing.info _, Navbar(Chair))
  }

  def info(paperId: Id[Paper]) = SlickAction(IsChair, _.alwaysEnabled) { implicit r => 
    Submitting.infoImpl(paperId, Some(routes.Chairing.edit(paperId)), Some(routes.Chairing.toggleWithdraw(paperId)), Navbar(Chair))
  }

  def toggleWithdraw(paperId: Id[Paper]) = SlickAction(IsChair, _.alwaysEnabled) { implicit r =>
    val paper: Paper = Query(r.db).paperWithId(paperId)
    r.connection insert paper.copy(withdrawn=(!paper.withdrawn))
    Redirect(routes.Chairing.info(paperId))
  }
  
  def edit(paperId: Id[Paper]) = SlickAction(IsChair, _.alwaysEnabled) { implicit r => 
    val form = Submitting.submissionForm.fill(SubmissionForm(Query(r.db) paperWithId paperId, Query(r.db) authorsOf paperId))
    Ok(views.html.submissionform("Editing Submission " + Query(r.db).indexOf(paperId), form, routes.Chairing.doEdit(paperId), Navbar(Chair)))
  }
  
  def doEdit(paperId: Id[Paper]) = SlickAction(IsChair, _.alwaysEnabled, parse.multipartFormData) { 
    implicit r => 
    Submitting.doSaveImpl(Some(paperId), routes.Chairing.doEdit(paperId), routes.Chairing.info, false)
  }
  
  def comment(paperId: Id[Paper]) = SlickAction(IsChair, _.alwaysEnabled) {
    implicit r => 
    Reviewing.doCommentImpl(paperId, routes.Chairing.doComment(paperId), Navbar(Chair))
  }
  
  def doComment(paperId: Id[Paper]) = SlickAction(IsChair, _.pcmemberComment) {
    implicit r => 
    Reviewing.commentForm.bindFromRequest.fold(_ => (),
      comment => r.connection insert comment.copy(paperId=paperId, personId=r.user.id))
    Redirect(routes.Chairing.comment(paperId))
  }

  def roles = SlickAction(IsChair, _.chairRoles) { implicit r =>
    val form = rolesForm fill RolesForm(Query(r.db).allPersonRoles.filterNot(_.personId == r.user.id))
    Ok(views.html.roles(form, Query(r.db).allPersons.toSet, Navbar(Chair)))
  }

  def doRoles = SlickAction(IsChair, _.chairRoles) { implicit r =>
    rolesForm.bindFromRequest.fold(_ => (), form => r.connection insert form.roles)
    Redirect(routes.Chairing.roles)
  }
}
