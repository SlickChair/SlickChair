package models

import Role.Reviewer
import play.api.db.slick.Config.driver.simple._
import scala.language.postfixOps

case class Phase(
  configuration: Configuration,
  email: Database => Option[Email] = _ => None,
  transitionCondition: Database => Boolean = _ => true
)

object Workflow {
  val phases: List[Phase] = List(
    Phase(Configuration("Setup", true, true, true, true, true, true, true, true)),
    
    Phase(Configuration("Submission", authorNewSubmission=true, authorEditSubmission=true)),
    
    Phase(Configuration("Bidding", pcmemberBid=true), { db: Database =>
      Some(Email(
        Query(db).reviewerEmails,
        "$conferenceShortName: Bidding phase begins",
        """Dear Program Committee Member,
           |
           |Submissions are closed it is now time for the bidding process to begin. You can go to $conferenceUrl to have a look at the submissions and indicate which papers you are willing to review and if you have any, your conflict of interest.
           |
           |Please complete these bids as soon as possible.
           |
           |Thanks for you help making $conferenceFullName a success!
           |
           |$conferenceShortName Program Chair
        """.stripMargin
      ))
    }),
    
    Phase(Configuration("Assignment", chairAssignment=true), transitionCondition={ db: Database =>
      Query(db).balancedAssignment
    }),
    
    Phase(Configuration("Review", pcmemberReview=true, pcmemberComment=true, chairDecision=true), {
      db: Database => Some(Email(
        Query(db).reviewerEmails,
        "$conferenceShortName: Submissions have been assigned for review",
        """Dear Program Committee Member,
           |
           |Submission assignments have been made and it is now time for the review process to begin. Go to $conferenceUrl to see the list submissions you have been assigned to review.
           |
           |Please complete these reviews as soon as possible.
           |
           |Thanks for you help making $conferenceFullName a success!
           |
           |$conferenceShortName Program Chair
        """.stripMargin
      ))
    }, transitionCondition={ db: Database =>
      Query(db).allReviewsCompleted
    }),
    
    Phase(Configuration("Decision", pcmemberComment=true, chairDecision=true), transitionCondition={
      db: Database =>
      Query(db).fullyDecided
    }),
    
    Phase(Configuration("Accepted notification"), { db: Database =>
      Some(Email(
        Query(db).acceptedEmails,
        "$conferenceShortName: Submission accepted",
        """Dear Author,
          |
          |On behalf of the $conferenceFullName, I am pleased to inform you that your submission has been accepted. Please find reviews of your submission at the following url: $conferenceUrl.
          |
          |Congratulations,
          |$conferenceShortName Program Committee
        """.stripMargin
      ))
    }),
    
    Phase(Configuration("Rejected notification"), { db: Database =>
      Some(Email(
        Query(db).rejectedEmails,
        "$conferenceShortName: Submission declined",
        """Dear Author,
          |
          |On behalf of the $conferenceFullName, I am sorry to inform you that your submission has not been accepted. We received many excellent submissions this year, and were limited in the number we could accept.
          |
          |You will find comments from the submission reviewers at the following url: $conferenceUrl. If you have questions about the comments, please contact the Chair.
          |
          |Sincerely,
          |$conferenceShortName Program Committee
        """.stripMargin
      ))
    })
  )
}
