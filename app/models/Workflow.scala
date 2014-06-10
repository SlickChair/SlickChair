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
  val setup = Configuration("Setup", true, true, true, true, true, true, true, true, true, true, false)
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
        transitionWarning="All submissions need a final acceptance decision."),
      
      Phase(
        Configuration("Done",
          chairCanRunSqlQueries=true,
          authorCanSeeReviews=true,
          showListOfAcceptedPapers=true),
        chairChangePhase("Done"))
    )
  }
}
