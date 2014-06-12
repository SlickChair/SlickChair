package controllers

import play.api.mvc.Flash
import play.api.Play.configuration
import play.api.Play.current
import play.api.mvc.RequestHeader
import securesocial.core.providers.utils.RoutesHelper
import securesocial.core.IdentityProvider

object Msg {
  private def error(message: String) = "error" -> message
  private def success(message: String) = "success" -> message
  private val conferenceFullName = configuration.getString("application.name").get
  private val conferenceShortName = configuration.getString("application.shortName").get

  def flash(s: (String, String)) = Flash(Map(s))
  
  object login {
    val checkEmail = success("Please check your email for further instructions.")
    val required = error("You need to log in to access that page.")
  }
  
  object chair {
    val assinged = success("Assignment saved.")
    val decided = success("Decision saved.")
    val role = success("User roles edited.")
    def phase(to: String) = success(s"Conference moved to the $to phase.")
    def phaseJump(to: String) = success(s"Conference jumped to the $to phase.")
  }

  object pcmember {
    val bided = success("Bids Saved.")
    val reviewError = error("There was an error with your review. Please try again.")
    val reviewed = success("Thanks for submitting your review!")
    val commented = success("Comment saved.")
    val edited = success("Review edited.")
  }

  object author {
    val editError = error("There was an error while trying edit this submission. Please try again.")
    val submitError = error("There was an error with your submission. Please try again.")
    val edited = success("This submission was successfully edited.")
    val submited = success("Thanks for your submission!")
    val withdrawn = success("Submission withdrawn.")
  }
  
  object subject {
    val accepted = s"$conferenceShortName: Submission accepted"
    val declined = s"$conferenceShortName: Submission declined"
    val reviewBeg = s"$conferenceShortName: Submissions have been assigned for review"
    val bidBeg = s"$conferenceShortName: Bidding phase begins"
    val passwordReset = s"$conferenceShortName: Password reset instructions"
    val signUp = s"$conferenceShortName: Sign up instructions"
    val submitted = s"$conferenceShortName: Submission received"
    def chairChangePhase(phase: String) = s"$conferenceShortName: $phase phase begins"
  }
  object email {
    def accepted(implicit r: RequestHeader) =
      s"""Dear Author,
        |
        |On behalf of the $conferenceFullName, I am pleased to inform you that your submission has been accepted. Please find reviews of your submission at the following url: ${routes.About.login.absoluteURL()}.
        |
        |Congratulations,
        |$conferenceShortName Program Committee
      """.trim.stripMargin
    def declined(implicit r: RequestHeader) =
      s"""Dear Author,
        |
        |On behalf of the $conferenceFullName, I am sorry to inform you that your submission has not been accepted. We received many excellent submissions this year, and were limited in the number we could accept.
        |
        |You will find comments from the submission reviewers at the following url: ${routes.About.login.absoluteURL()}. If you have questions about the comments, please contact the Chair.
        |
        |Sincerely,
        |$conferenceShortName Program Committee
      """.trim.stripMargin
    def reviewBeg(implicit r: RequestHeader) =
      s"""Dear Program Committee Member,
         |
         |Submission assignments have been made and it is now time for the review process to begin. Go to ${routes.About.login.absoluteURL()} to see the list submissions you have been assigned to review.
         |
         |Please complete these reviews as soon as possible.
         |
         |Thanks for you help making $conferenceFullName a success!
         |
         |$conferenceShortName Program Chair
      """.trim.stripMargin
    def bidBeg(implicit r: RequestHeader) = 
      s"""Dear Program Committee Member,
         |
         |The system is now closed for submissions. It is time to start the bidding process. Please go to ${routes.About.login.absoluteURL()} and indicate the papers you are willing to review. Please also indicate all your conflicts of interest.
         |
         |Please complete these bids as soon as possible.
         |
         |Thanks for you help making $conferenceFullName a success!
         |
         |$conferenceShortName Program Chair
      """.trim.stripMargin
    def passwordReset(token: String)(implicit r: RequestHeader) = 
      s"""Hello
         |
         |Please follow this link to reset your password.
         |
         |${RoutesHelper.resetPassword(token).absoluteURL(IdentityProvider.sslEnabled)}
         |
         |Best regards,
         |$conferenceShortName Program Committee
      """.trim.stripMargin
    def signUp(token: String)(implicit r: RequestHeader) =
      s"""Hello
         |
         |Please follow this link to complete your registration:
         |
         |${RoutesHelper.signUp(token).absoluteURL(IdentityProvider.sslEnabled)}
         |
         |Best regards,
         |$conferenceShortName Program Committee
      """.trim.stripMargin
    def submitted(infoUrl: String) =
      s"""Dear Author,
         |
         |Thank you for submitting to $conferenceFullName. Follow this link to view your submission:
         |
         |$infoUrl
         |
         |Best regards,
         |$conferenceShortName Program Committee
       """.trim.stripMargin
     def chairChangePhase(phase: String) =
      s"""Dear Chair,
         |
         |The conference has advanced to the $phase phase.
       """.trim.stripMargin
   }
}
