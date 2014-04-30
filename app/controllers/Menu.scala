package controllers

import play.api.db.slick.Config.driver.simple._
import play.api.mvc.Call
import models._
import models.PersonRole._

object Menu {
  private val newSubmission = (routes.Submitting.make, "New Submission")  
  private val home = (chair.routes.Doc.umentation, "SlickChair Demo")
  
  def apply(user: models.Person)(implicit session: Session): List[(Call, String)] = {
    home :: (user.role match {
      case Submitter =>
        val papers = Papers of user.email map (id =>
          (routes.Submitting.info(id.value), "Submission " + id.value.toString.take(4).toUpperCase))
        newSubmission :: papers
      case Reviewer =>
        Nil
      case Chair =>
       List((chair.routes.Sql.form, "SQL"))
    })
  }
  
  def notLoggedIn: List[(Call, String)] = List(home, newSubmission)
}
