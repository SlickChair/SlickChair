package controllers

import models._
import Role.Chair
import Evaluation.Evaluation
import Decision.{Decision, Undecided}
import play.api.mvc.Controller
import Mappers.{enumFormMapping, idFormMapping}
import play.api.data.Mapping
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, boolean, text}
import play.api.data.Mapping
import BidValue.Maybe
import play.api.Play.current
import play.api.libs.concurrent.Akka
import com.typesafe.plugin._
import scala.concurrent.duration._

case class AssignmentForm(assignments: List[Assignment])
case class DecisionForm(decisions: List[PaperDecision])
case class RolesForm(roles: List[PersonRole])
case class JumpToPhaseForm(configurationName: String)

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

  def emailForm: Form[Email] = Form(mapping(
    "to" -> text,
    "subject" -> text,
    "content" -> text,
    "metadata" -> ignored(newMetadata[Email])
  )(Email.apply _)(Email.unapply _))

  def jumpToPhaseForm: Form[JumpToPhaseForm] = Form(mapping(
    "configurationName" -> text
  )(JumpToPhaseForm.apply _)(JumpToPhaseForm.unapply _))

  def assignmentList() = SlickAction(IsChair, _ => true) { implicit r =>
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
    assignmentForm.bindFromRequest.fold(_ => (),
      form => { r.connection insert (form.assignments map { _ copy (paperId=paperId) }) }
    )
    Redirect(routes.Chairing.assign(paperId))
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
  
  def submissions = SlickAction(IsChair, _ => true) { implicit r => 
    Reviewing.submissionsImpl(routes.Chairing.info _, Navbar(Chair))
  }

  def info(paperId: Id[Paper]) = SlickAction(IsChair, _ => true) { implicit r => 
    Submitting.infoImpl(paperId, Some(routes.Chairing.edit(paperId)), Some(routes.Chairing.toggleWithdraw(paperId)), Navbar(Chair))
  }

  def toggleWithdraw(paperId: Id[Paper]) = SlickAction(IsChair, _ => true) { implicit r =>
    val paper: Paper = Query(r.db).paperWithId(paperId)
    r.connection insert paper.copy(withdrawn=(!paper.withdrawn))
    Redirect(routes.Chairing.info(paperId))
  }
  
  def edit(paperId: Id[Paper]) = SlickAction(IsChair, _ => true) { implicit r => 
    val form = Submitting.submissionForm.fill(SubmissionForm(Query(r.db) paperWithId paperId, Query(r.db) authorsOf paperId))
    Ok(views.html.submissionform("Editing Submission " + Query(r.db).indexOf(paperId), form, routes.Chairing.doEdit(paperId), Navbar(Chair)))
  }
  
  def doEdit(paperId: Id[Paper]) = SlickAction(IsChair, _ => true, parse.multipartFormData) { 
    implicit r => 
    Submitting.doSaveImpl(Some(paperId), routes.Chairing.doEdit(paperId), routes.Chairing.info, false)
  }
  
  def comment(paperId: Id[Paper]) = SlickAction(IsChair, _ => true) {
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
  
  def phases = SlickAction(IsChair, _ => true) { implicit r =>
    val currentConf = Query(r.db).configuration
    val currentPhase = Workflow.phases.find(_.configuration.name == currentConf.name)
    
    Ok(views.html.phases(currentConf,
      currentPhase filterNot (_ transitionCondition r.db) map (_.transitionReason),
      currentPhase flatMap (_ email r.db) map (emailForm fill _) getOrElse emailForm,
      currentPhase.map(p => jumpToPhaseForm.fill(JumpToPhaseForm(p.configuration.name))).getOrElse(jumpToPhaseForm),
      Workflow.phases.map(_.configuration), Navbar(Chair)))
  }
  
  def doPhases = SlickAction(IsChair, _ => true) { implicit r =>
    emailForm.bindFromRequest.fold(_ => (),
      form => {
        import play.api.libs.concurrent.Execution.Implicits._
        Akka.system.scheduler.scheduleOnce(1.seconds) {
          val mail = use[MailerPlugin].email
          mail.setSubject(form.subject)
          mail.setRecipient(form.to.split(","): _*)
          mail.setFrom(current.configuration.getString("smtp.from").get)
          mail.send(form.content)
        }
      }
    )
    val currentConf = Query(r.db).configuration
    Workflow.phases.dropWhile(_.configuration.name != currentConf.name) match {
      case _ :: nextPhase :: _ => r.connection insert nextPhase.configuration
      case _ => ()
    }
    Redirect(routes.Chairing.phases)
  }
  
  def jumpToPhase = SlickAction(IsChair, _ => true) { implicit r =>
    jumpToPhaseForm.bindFromRequest.fold(_ => (),
      form => {
        Workflow.phases map (_.configuration) find (_.name == form.configurationName) map {
          r.connection insert _
        }
      }
    )
    Redirect(routes.Chairing.phases)
  }
}
