package controllers.member

import org.joda.time.DateTime
import controllers.MemberOrChair
import play.api.data.Form
import play.api.data.Forms.{ ignored, mapping, nonEmptyText }
import play.api.data.Mapping
import play.api.mvc.{ Controller, Result }
import securesocial.core.SecureSocial

object Reviewing extends Controller with SecureSocial {
  
  def papers() = SecuredAction { implicit request =>
    Ok("")
  }

  // val confidenceMapping: Mapping[ReviewConfidence] = mapping(
  //   "value" -> nonEmptyText)(ReviewConfidence.withName(_))(Some(_).map(_.toString))

  // val evaluationMapping: Mapping[ReviewEvaluation] = mapping(
  //   "value" -> nonEmptyText)(ReviewEvaluation.withName(_))(Some(_).map(_.toString))

  // val reviewForm = Form[Review] (
  //   mapping(
  //     "paperid" -> ignored(null.asInstanceOf[Int]),
  //     "memberid" -> ignored(null.asInstanceOf[Int]),
  //     "submissiondate" -> ignored(null.asInstanceOf[Option[DateTime]]),
  //     "lastupdate" -> ignored(null.asInstanceOf[Option[DateTime]]),
  //     "confidence" -> confidenceMapping,
  //     "evaluation" -> evaluationMapping,
  //     "content" -> nonEmptyText
  //   )(Review.apply _)(Review.unapply _)
  // )
  
  // val commentForm = Form[Comment] (
  //   mapping(
  //     "id" -> ignored(null.asInstanceOf[Int]),
  //     "paperid" -> ignored(null.asInstanceOf[Int]),
  //     "memberid" -> ignored(null.asInstanceOf[Int]),
  //     "submissiondate" -> ignored(null.asInstanceOf[DateTime]),
  //     "content" -> nonEmptyText
  //   )(Comment.apply _)(Comment.unapply _)
  // )
  
  // private def paperOrNotFound(id: Int)(ifFound: Paper => Result) = Papers.withId(id) match {
  //   case None => NotFound("No paper found with this id")
  //   case Some(paper) => ifFound(paper)
  // }
    
  // def page(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
  //   paperOrNotFound(id) { paper =>
  //     Ok(views.html.member.paperPage(
  //       Reviews.of(paper, Persons.getFromRequest).nonEmpty,
  //       paper,
  //       Authors.of(paper),
  //       Reviews.ofPaper(paper).map(r => (r, Persons.withId(r.memberid).get)),
  //       Comments.ofPaper(paper).map(r => (r, Persons.withId(r.memberid).get)),
  //       commentForm
  //     ))
  //   }
  // }
  
  // def form(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
  //   paperOrNotFound(id) { paper =>
  //     Reviews.of(paper, Persons.getFromRequest) match {
  //       case None =>
  //         Ok(views.html.member.paperReview(false, paper, Authors.of(paper), reviewForm))
  //       case Some(review) => 
  //         Ok(views.html.member.paperReview(true, paper, Authors.of(paper), reviewForm.fill(review)))
  //     }
  //   }
  // }
  
  // def make(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
  //   val member = Persons.getFromRequest
  //   paperOrNotFound(id) { paper =>
  //     reviewForm.bindFromRequest.fold(
  //       errors =>
  //         Ok(views.html.member.paperReview(Reviews.of(paper, member).nonEmpty, paper, Authors.of(paper), errors)),
  //       form => {
  //         val now = DateTime.now
  //         Reviews.of(paper, member) match {
  //           case None => Reviews.ins(form.copy(
  //             paperid = paper.id,
  //             memberid = member.id,
  //             submissiondate = Some(now),
  //             lastupdate = Some(now)
  //           ))
  //           case Some(dbReview) => Reviews.updt(form.copy(
  //             paperid = dbReview.paperid,
  //             memberid = dbReview.memberid,
  //             submissiondate = dbReview.submissiondate,
  //             lastupdate = Some(now)
  //           ))
  //         }
  //         Redirect(routes.Reviewing.page(paper.id))
  //       }
  //     )
  //   }
  // }
  
  // def comment(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
  //   paperOrNotFound(id) { paper =>
  //     commentForm.bindFromRequest.fold(
  //       errors => Unit,
  //       form => {
  //         Comments.ins(NewComment(
  //           paper.id,
  //           Persons.getFromRequest.id,
  //           DateTime.now,
  //           form.content
  //         ))
  //       }
  //     )
  //     Redirect(routes.Reviewing.page(paper.id))
  //   }
  // }
}
