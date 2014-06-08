package controllers

import play.api.mvc.Flash
import play.api.http.HeaderNames._

object Msg {
  private def error(message: String) = "error" -> message
  private def success(message: String) = "success" -> message
  
  def flash(s: (String, String)) = Flash(Map(s))
  
  object chair {
  }

  object pcmember {
  }

  object author {
    val editError = error("There was an error while trying edit your submission. Please try again.")
    val submitError = error("There was an error with you submission. Please try again.")
    val edited = success("Your submission was successfully edited.")
    val submited = success("Thank you for your submission!")
    val withdrawn = success("Your submission was successfully withdrawn.")
  }
}
  
