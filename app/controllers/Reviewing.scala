package controllers

import org.joda.time.DateTime
import models.secureSocial._
import models.entities._
import models.relations.{Comment, Comments, NewComment, Review, ReviewConfidence}
import models.relations.ReviewConfidence.ReviewConfidence
import models.relations.ReviewEvaluation
import models.relations.ReviewEvaluation.ReviewEvaluation
import models.relations.Reviews
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, nonEmptyText}
import play.api.data.Mapping
import play.api.mvc.{Controller, Result}
import securesocial.core.{SecureSocial, SecuredRequest}

object Reviewing extends Controller with SecureSocial {
  val confidenceMapping: Mapping[ReviewConfidence] = mapping(
    "value" -> nonEmptyText)(ReviewConfidence.withName(_))(Some(_).map(_.toString))

  val evaluationMapping: Mapping[ReviewEvaluation] = mapping(
    "value" -> nonEmptyText)(ReviewEvaluation.withName(_))(Some(_).map(_.toString))

  val reviewForm = Form[Review] (
    mapping(
      "paperid" -> ignored(null.asInstanceOf[Int]),
      "memberid" -> ignored(null.asInstanceOf[Int]),
      "submissiondate" -> ignored(null.asInstanceOf[Option[DateTime]]),
      "lastupdate" -> ignored(null.asInstanceOf[Option[DateTime]]),
      "confidence" -> confidenceMapping,
      "evaluation" -> evaluationMapping,
      "content" -> nonEmptyText
    )(Review.apply _)(Review.unapply _)
  )
  
  val commentForm = Form[Comment] (
    mapping(
      "id" -> ignored(null.asInstanceOf[Int]),
      "paperid" -> ignored(null.asInstanceOf[Int]),
      "memberid" -> ignored(null.asInstanceOf[Int]),
      "submissiondate" -> ignored(null.asInstanceOf[DateTime]),
      "content" -> nonEmptyText
    )(Comment.apply _)(Comment.unapply _)
  )
  
  def papers = SecuredAction(MemberOrChair) { implicit request =>
    Ok(views.html.paperList(
      Papers.all.map(p => (p, Authors.of(p)))
    ))
  }
  
  private def paperOrNotFound(id: Int)(ifFound: Paper => Result) = Papers.withId(id) match {
    case None => NotFound("No paper found with this id")
    case Some(paper) => ifFound(paper)
  }
  
  private def getMember[T](implicit request: SecuredRequest[T]) =
    // The last get won't fail if called after the MemberOrChair authentication succeeded.
    Members.withEmail(request.user.email.get).get
  
  def page(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
    paperOrNotFound(id) { paper =>
      Ok(views.html.paperPage(
        Reviews.of(paper, getMember).nonEmpty,
        paper,
        Authors.of(paper),
        Reviews.ofPaper(paper).map(r => (r, Members.withId(r.memberid).get)),
        Comments.ofPaper(paper).map(r => (r, Members.withId(r.memberid).get)),
        commentForm
      ))
    }
  }
  
  def form(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
    paperOrNotFound(id) { paper =>
      Reviews.of(paper, getMember) match {
        case None =>
          Ok(views.html.paperReview(false, paper, Authors.of(paper), reviewForm))
        case Some(review) => 
          Ok(views.html.paperReview(true, paper, Authors.of(paper), reviewForm.fill(review)))
      }
    }
  }
  
  def make(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
    val member = getMember
    paperOrNotFound(id) { paper =>
      reviewForm.bindFromRequest.fold(
        errors =>
          Ok(views.html.paperReview(Reviews.of(paper, member).nonEmpty, paper, Authors.of(paper), errors)),
        form => {
          val now = DateTime.now
          Reviews.of(paper, member) match {
            case None => Reviews.ins(form.copy(
              paperid = paper.id,
              memberid = member.id,
              submissiondate = Some(now),
              lastupdate = Some(now)
            ))
            case Some(dbReview) => Reviews.updt(form.copy(
              paperid = dbReview.paperid,
              memberid = dbReview.memberid,
              submissiondate = dbReview.submissiondate,
              lastupdate = Some(now)
            ))
          }
          Redirect(routes.Reviewing.page(paper.id))
        }
      )
    }
  }
  
  def comment(id: Int) = SecuredAction(MemberOrChair) { implicit request =>
    paperOrNotFound(id) { paper =>
      commentForm.bindFromRequest.fold(
        errors => Unit,
        form => {
          Comments.ins(NewComment(
            paper.id,
            getMember.id,
            DateTime.now,
            form.content
          ))
        }
      )
      Redirect(routes.Reviewing.page(paper.id))
    }
  }
  
  def invite(uuid: String) = SecuredAction(Anyone) { implicit request =>
    SecureSocialTokens.withUUID(uuid) match {
      case None => BadRequest("Token expired.")
      case Some(token) => 
        val user = User.fromIdentity(request.user)
        Members.withEmail(user.email) match {
          case Some(_) => BadRequest("Already a member.")
          case None => 
            val now = DateTime.now
            Members.ins(NewMember(
              user.email,
              token.email,
              now,
              now,
              MemberRole.Member,
              user.firstname,
              user.lastname
            ))
            Redirect(routes.Reviewing.dashboard)
        }
    }
  }
  
  def dashboard = TODO
}