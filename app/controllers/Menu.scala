package controllers

import play.api.db.slick.Config.driver.simple._
import play.api.mvc.Call
import models._

object Menu {
  def apply(user: String)(implicit session: Session): List[(Call, String)] = {
    val papers = Papers of user map (id =>
      (routes.Submitting.info(id.value), "Submission " + id.value.toString.take(4).toUpperCase))
    val adminStuff = List((chair.routes.Sql.form, "SQL"))
    List(notLoggedIn, papers, adminStuff).flatten
  }
  
  def notLoggedIn: List[(Call, String)] = List(
    (chair.routes.Doc.umentation, "SlickChair Demo"),
    (routes.Submitting.make, "New Submission")  
  )
}
