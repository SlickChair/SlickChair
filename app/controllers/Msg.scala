package controllers

import play.api.mvc.Flash
import play.api.http.HeaderNames._

object Msg {
  private def error(message: String) = "error" -> message
  private def success(message: String) = "success" -> message
  
  def flash(s: (String, String)) = Flash(Map(s))
  
  object chair {
    val assinged = success("Assignment saved.")
    val decided = success("Acceptance decision saved.")
    val role = success("User roles edited.")
    val phase = success("Conference moved to the next phase.")
    val phaseJump = success("Conference phase changed.")
  }

  object pcmember {
    val bided = success("Bids Saved.")
    val reviewError = error("There was an error with your review. Please try again.")
    val reviewed = success("Thanks for submitting your review!")
    val commented = success("Comment saved.")
    val edited = success("Review edited.")
  }

  object author {
    val editError = error("There was an error while trying edit your submission. Please try again.")
    val submitError = error("There was an error with your submission. Please try again.")
    val edited = success("Your submission was successfully edited.")
    val submited = success("Thanks for your submission!")
    val withdrawn = success("Submission withdrawn.")
  }
}
