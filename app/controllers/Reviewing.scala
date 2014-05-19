package controllers

import org.joda.time.DateTime
import play.api.data.{ Form, Mapping }
import play.api.data.Forms.{ ignored, mapping, nonEmptyText, list }
import play.api.mvc.{ Controller, Result }
import play.api.db.slick.DB
import play.api.Play.current
import play.api.templates.Html
import models.PersonRole.Reviewer
import models._
import models.BidValue._
import models.ReviewConfidence._
import models.ReviewEvaluation._
import Utils._

case class BidForm(bids: List[Bid])

object Reviewing extends Controller {
  def bidMapping: Mapping[Bid] = mapping(
    "paperid" -> idMapping[Paper],
    "personid" -> ignored(newMetadata[Person]._1),
    "bid" -> enumMapping(BidValue),
    "metadata" -> ignored(newMetadata[Bid])
  )(Bid.apply _)(Bid.unapply _)

  def bidForm: Form[BidForm] = Form(
    mapping("bids" -> list(bidMapping))
    (BidForm.apply _)(BidForm.unapply _)
  )
  
  def reviewForm: Form[Review] = Form(mapping(
    "paperid" -> ignored(newMetadata[Paper]._1),
    "personid" -> ignored(newMetadata[Person]._1),
    "confidence" -> enumMapping(ReviewConfidence),
    "evaluation" -> enumMapping(ReviewEvaluation),
    "content" -> nonEmptyText,
    "metadata" -> ignored(newMetadata[Review])
  )(Review.apply _)(Review.unapply _))
  
  def commentForm: Form[Comment] = Form(mapping(
    "paperid" -> ignored(newMetadata[Paper]._1),
    "personid" -> idMapping[Person],
    "content" -> nonEmptyText,
    "metadata" -> ignored(newMetadata[Comment])
  )(Comment.apply _)(Comment.unapply _))

  def bid() = SlickAction(IsReviewer) { implicit r =>
    val bids: List[Bid] = Query(r.db) bidsOf r.user.id
    val papers: List[Paper] = Query(r.db).allPapers
    val allBids: List[Bid] = papers map { p =>
      bids.find(_.paperid == p.id) match {
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
        val bids = form.bids map { _ copy (personid=r.user.id) }
        r.connection.insert(bids)
        Redirect(routes.Reviewing.bid)
      }
    )
  }

  def papers() = SlickAction(IsReviewer) { implicit r =>
    Ok(views.html.main("List of all submissions", Navbar(Reviewer))(Html(
      Query(r.db).allPapers.toString.replaceAll(",", ",\n<br>"))))
  }
  
  // def review(id: IdType, form: Form[Review] = reviewForm) = SlickAction(NonConflictingReviewer(id)) { implicit r =>
  def review(id: IdType) = SlickAction(NonConflictingReviewer(id)) { implicit r =>
    val paperId: Id[Paper] = Id[Paper](id)
    val paper: Paper = Query(r.db) paperWithId paperId
    if(Query(r.db).notReviewed(r.user.id, paperId))
      Ok(views.html.review("Submission " + Query(r.db).indexOf(paper.id), reviewForm, paper, Navbar(Reviewer))(Submitting.summary(paper.id)))
    else
      Ok(views.html.comment("Submission " + Query(r.db).indexOf(paper.id), commentForm, Query(r.db).commentsOf(paper.id), Query(r.db).reviewsOf(paper.id), paper, Query(r.db).allStaff.toSet, Navbar(Reviewer))(Submitting.summary(paper.id)))
  }
  
  def doReview(id: IdType) = SlickAction(NonConflictingReviewer(id)) { implicit r =>
    reviewForm.bindFromRequest.fold(
      errors => {
        // review(id, errors)(r), // TODO: DRY with this, use Action.async everywhere...
        val paper: Paper = Query(r.db) paperWithId Id[Paper](id)
        Ok(views.html.review("Submission " + Query(r.db).indexOf(paper.id), errors, paper, Navbar(Reviewer))(Submitting.summary(paper.id)))
      },
      review => {
        r.connection insert List(review.copy(paperid=Id[Paper](id), personid=r.user.id))
        Redirect(routes.Reviewing.review(id))
      }
    )
  }
  
  def doComment(id: IdType) = SlickAction(NonConflictingReviewer(id)) { implicit r =>
    Ok("")
  }

  def editComment(pid: IdType, cid: IdType) = SlickAction(NonConflictingReviewer(pid)) {
    implicit r =>
    Ok("")
  }

  def editReview(pid: IdType, rid: IdType) = SlickAction(NonConflictingReviewer(pid)) {
    implicit r =>
    Ok("")
  }
}
