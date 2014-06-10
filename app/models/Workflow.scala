package models

import Role.PC_Member
import play.api.db.slick.Config.driver.simple._
import scala.language.postfixOps
import controllers.Msg.{email, subject}
import play.api.mvc.RequestHeader

case class Phase(
  configuration: Configuration,
  email: Database => Email,
  secondEmail: Database => Option[Email] = _ => None,
  transitionGuard: Database => Boolean = _ => true,
  transitionWarning: String = ""
)

object Workflow {
  def setup = Configuration("Setup", 
    chairCanChangeRoles=true,
    chairCanAssignSubmissions=true,
    chairCanDecideOnAcceptance=true,
    chairCanRunSqlQueries=false,
    pcmemberCanBid=true,
    pcmemberCanReview=true,
    pcmemberCanComment=true,
    authorCanMakeNewSubmissions=true,
    authorCanEditSubmissions=true,
    authorCanSeeReviews=true,
    showListOfAcceptedPapers=false)

  def chairChangePhase(to: String) = { db: Database =>
    Email(Query(db).chairEmails mkString ", ", subject.chairChangePhase(to), email.chairChangePhase(to))
  }
  
  def phases(implicit r: RequestHeader): List[Phase] = {
    List(
      Phase(setup, chairChangePhase("Submission")),
      Phase(
        Configuration("Submission",
          authorCanMakeNewSubmissions=true,
          authorCanEditSubmissions=true),
        { db => Email(Query(db).reviewerEmails mkString ", ", subject.bidBeg, email.bidBeg) }),
      
      Phase(Configuration("Bidding",
        pcmemberCanBid=true),
        chairChangePhase("Assignment")),
              
      Phase(
        Configuration("Assignment",
          chairCanAssignSubmissions=true),
        { db => Email(Query(db).reviewerEmails mkString ", ", subject.reviewBeg, email.reviewBeg) }),
      
      Phase(
        Configuration("Review",
          pcmemberCanReview=true,
          pcmemberCanComment=true,
          chairCanDecideOnAcceptance=true),
        chairChangePhase("Notification")),
      
      Phase(
        Configuration("Notification",
          pcmemberCanComment=true,
          chairCanDecideOnAcceptance=true),
        { db => Email(Query(db).acceptedEmails mkString ", ", subject.accepted, email.accepted) },
        { db => Some(Email(Query(db).rejectedEmails mkString ", ", subject.declined, email.declined)) },
        transitionGuard={ db => Query(db).fullyDecided },
        transitionWarning="Some submissions have a temporary decision. Make sure all submissions are either Accepted or Rejected before sending notifications, or some authors will not be notified."),
      
      Phase(
        Configuration("Done",
          chairCanRunSqlQueries=true,
          authorCanSeeReviews=true,
          showListOfAcceptedPapers=true),
        chairChangePhase("Done"))
    )
  }
}
