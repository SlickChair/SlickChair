package controllers

import models._
import BidValue._
import Role.Reviewer
import Mappers.{enumFormMapping, idFormMapping}
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, nonEmptyText}
import play.api.data.Mapping
import play.api.mvc.{Call, Controller}
import play.api.templates.Html

case class BidForm(bids: List[Bid])

object Reviewing extends Controller {
  def bidFormMapping: Mapping[Bid] = mapping(
    "paperId" -> idFormMapping[Paper],
    "personId" -> ignored(newMetadata[Person]._1),
    "bid" -> enumFormMapping(BidValue),
    "metadata" -> ignored(newMetadata[Bid])
  )(Bid.apply _)(Bid.unapply _)

  def bidForm: Form[BidForm] = Form(
    mapping("bids" -> list(bidFormMapping))
    (BidForm.apply _)(BidForm.unapply _)
  )
  
  def reviewForm: Form[Review] = Form(mapping(
    "paperId" -> ignored(newMetadata[Paper]._1),
    "personId" -> ignored(newMetadata[Person]._1),
    "confidence" -> enumFormMapping(Confidence),
    "evaluation" -> enumFormMapping(Evaluation),
    "content" -> nonEmptyText,
    "metadata" -> ignored(newMetadata[Review])
  )(Review.apply _)(Review.unapply _))
  
  def commentForm: Form[Comment] = Form(mapping(
    "paperId" -> ignored(newMetadata[Paper]._1),
    "personId" -> ignored(newMetadata[Person]._1),
    "content" -> nonEmptyText,
    "metadata" -> ignored(newMetadata[Comment])
  )(Comment.apply _)(Comment.unapply _))

  def bid() = SlickAction(IsReviewer) { implicit r =>
    val bids: List[Bid] = Query(r.db) bidsOf r.user.id
    val papers: List[Paper] = Query(r.db).allPapers
    val allBids: List[Bid] = papers map { p =>
      bids.find(_.paperId == p.id) match {
        case None => Bid(p.id, r.user.id, Maybe)
        case Some(b) => b
      }
    }
    val form = bidForm fill BidForm(allBids)
    Ok(views.html.bid(form, papers.toSet, Query(r.db).allFiles.toSet, Navbar(Reviewer)))
  }

  def doBid() = SlickAction(IsReviewer) { implicit r =>
    bidForm.bindFromRequest.fold(
      errors => 
        Ok(views.html.bid(errors, Query(r.db).allPapers.toSet,  Query(r.db).allFiles.toSet, Navbar(Reviewer))),
      form => {
        val bids = form.bids map { _ copy (personId=r.user.id) }
        r.connection.insert(bids)
        Redirect(routes.Reviewing.bid)
      }
    )
  }

  def papers() = SlickAction(IsReviewer) { implicit r =>
    Ok(views.html.main("List of all submissions", Navbar(Reviewer))(Html(
      Query(r.db).allPapers.toString.replaceAll(",", ",\n<br>"))))
  }
  
  def review(paperId: Id[Paper]) = SlickAction(NonConflictingReviewer(paperId)) { implicit r =>
    if(Query(r.db).notReviewed(r.user.id, paperId))
      Ok(views.html.review("Submission " + Query(r.db).indexOf(paperId), reviewForm, Query(r.db).paperWithId(paperId), Navbar(Reviewer))(Submitting.summary(paperId)))
    else
      comment(paperId, routes.Reviewing.doComment(paperId), Navbar(Reviewer))
  }
  
  def comment(paperId: Id[Paper], doCommentEP: Call, navbar: Html)(implicit r: SlickRequest[_]) = {
    Ok(views.html.comment("Submission " + Query(r.db).indexOf(paperId), commentForm.fill(Comment(paperId, r.user.id, "")), reviewForm, Query(r.db).commentsOf(paperId), Query(r.db).reviewsOf(paperId), Query(r.db).paperWithId(paperId), r.user, Query(r.db).allStaff.toSet, doCommentEP, navbar)(Submitting.summary(paperId)))
  }
  
  def doReview(paperId: Id[Paper]) = SlickAction(NonConflictingReviewer(paperId)) { implicit r =>
    reviewForm.bindFromRequest.fold(
      errors => {
        // review(id, errors)(r), // TODO: DRY with this, use Action.async everywhere...
        val paper: Paper = Query(r.db) paperWithId paperId
        Ok(views.html.review("Submission " + Query(r.db).indexOf(paperId), errors, paper, Navbar(Reviewer))(Submitting.summary(paperId)))
      },
      review => {
        r.connection insert List(review.copy(paperId=paperId, personId=r.user.id))
        Redirect(routes.Reviewing.review(paperId))
      }
    )
  }
  
  def doComment(paperId: Id[Paper]) = SlickAction(NonConflictingReviewer(paperId)) { implicit r =>
    commentForm.bindFromRequest.fold(_ => (),
      comment => r.connection insert List(comment.copy(paperId=paperId, personId=r.user.id)))
    Redirect(routes.Reviewing.review(paperId))
  }

  // def editComment(paperId: Id[Paper], commentId: Id[Comment], personId: Id[Person]) = SlickAction(NonConflictingReviewer(paperId)) { implicit r =>
  //   commentForm.bindFromRequest.fold(_ => (),
  //     comment => {
  //       r.connection insert List(
  //         comment.copy(paperId=paperId, personId=personId, metadata=withId(commentId))
  //       )
  //     }
  //   )
  //   Redirect(routes.Reviewing.review(paperId))
  // }

  def editReview(paperId: Id[Paper], personId: Id[Person]) = SlickAction(NonConflictingReviewer(paperId)) {
      implicit r =>
    reviewForm.bindFromRequest.fold(_ => (),
      review => {
        r.connection insert List(review.copy(paperId=paperId, personId=personId))
      }
    )
    Redirect(routes.Reviewing.review(paperId))
  }
}
