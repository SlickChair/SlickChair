package models

import Role.PC_Member
import play.api.db.slick.Config.driver.simple._
import scala.language.postfixOps
import controllers.Msg.{email, subject}
import play.api.mvc.RequestHeader

case class Phase(
  configuration: Configuration,
  email: Database => Option[Email] = _ => None,
  transitionCondition: Database => Boolean = _ => true,
  transitionReason: String = ""
)

object Workflow {
  val setup = Configuration("Setup", true, true, true, true, true, true, true, true, true)
  
  def phases(implicit r: RequestHeader): List[Phase] = {
    List(
      Phase(setup),
      Phase(
        Configuration("Submission", authorNewSubmission=true, authorEditSubmission=true),
        { db => Some(Email(Query(db).reviewerEmails mkString ", ", subject.bidBeg, email.bidBeg)) }
      ),
      Phase(Configuration("Bidding", pcmemberBid=true)),
      Phase(
        Configuration("Assignment", chairAssignment=true),
        { db => Some(Email(Query(db).reviewerEmails mkString ", ", subject.reviewBeg, email.reviewBeg)) },
        transitionCondition={ db => Query(db).balancedAssignment },
        transitionReason="All submissions need to have the same number of assignment."
      ),
      Phase(
        Configuration("Review", pcmemberReview=true, pcmemberComment=true, chairDecision=true),
        transitionCondition={ db => Query(db).allReviewsCompleted },
        transitionReason="All reviews have to be been completed."
      ),
      Phase(
        Configuration("Decision", pcmemberComment=true, chairDecision=true),
        { db => Some(Email(Query(db).acceptedEmails mkString ", ", subject.accepted, email.accepted)) },
        transitionCondition={ db => Query(db).fullyDecided },
        transitionReason="All submissions need a final acceptance decision."
      ),
      Phase(
        Configuration("Notification"), { db =>
        Some(Email(Query(db).rejectedEmails mkString ", ", subject.declined, email.declined))
      }),
      Phase(Configuration("Done"))
    )
  }
}
