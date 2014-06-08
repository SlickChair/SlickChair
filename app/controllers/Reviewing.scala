package controllers

import models._
import BidValue._
import Role.PC_Member
import Confidence.Confidence
import Evaluation.Evaluation
import Mappers.{enumFormMapping, idFormMapping}
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, nonEmptyText}
import play.api.data.Mapping
import play.api.mvc.{Call, Controller}
import play.api.templates.Html
import org.joda.time.DateTime

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
    "metadat" -> ignored(newMetadata[Review])
  )(Review.apply _)(Review.unapply _))
  
  def commentForm: Form[Comment] = Form(mapping(
    "paperId" -> ignored(newMetadata[Paper]._1),
    "personId" -> ignored(newMetadata[Person]._1),
    "content" -> nonEmptyText,
    "metadata" -> ignored(newMetadata[Comment])
  )(Comment.apply _)(Comment.unapply _))

  def bid() = SlickAction(IsPCMember, _.pcmemberBid) { implicit r =>
    val bids: List[Bid] = Query(r.db) bidsOf r.user.id
    val papers: List[Paper] = Query(r.db).allPapers
    val allBids: List[Bid] = papers map { p =>
      bids.find(_.paperId == p.id) match {
        case None => Bid(p.id, r.user.id, Maybe)
        case Some(b) => b
      }
    }
    val form = bidForm fill BidForm(allBids)
    Ok(views.html.bid(form, papers.toSet, Query(r.db).allFiles.toSet, Navbar(PC_Member)))
  }

  def doBid() = SlickAction(IsPCMember, _.pcmemberBid) { implicit r =>
    bidForm.bindFromRequest.fold(
      errors => 
        Ok(views.html.bid(errors, Query(r.db).allPapers.toSet,  Query(r.db).allFiles.toSet, Navbar(PC_Member))),
      form => {
        val bids = form.bids map { _ copy (personId=r.user.id) }
        r.connection insert bids
        Redirect(routes.Reviewing.bid)
      }
    )
  }

  def submissions = SlickAction(IsPCMember, _ => true) { implicit r =>
    submissionsImpl(routes.Reviewing.review _, Navbar(PC_Member))
  }
  
  def submissionsImpl(infoEP: Id[Paper] => Call, navbar: Html)(implicit r: SlickRequest[_]) = {
    val files: List[File] = Query(r.db).allFiles
    val papers: List[Paper] = Query(r.db).nonConflictingPapers(r.user.id)
    val indexOf: Id[Paper] => Int = paperId =>
      Query(r.db).allPaperIndices.map(_.paperId).zipWithIndex.find(_._1 == paperId).get._2
    val rows: List[(Paper, Int, Option[File])] = papers map { paper =>
      (paper, indexOf(paper.id), paper.fileId.map(id => files.find(_.id == id).get))
    }
    Ok(views.html.submissionlist(rows, infoEP, navbar))
  }

  def review(paperId: Id[Paper]) = SlickAction(NonConflictingPCMember(paperId), _ => true) {
    implicit r =>
    val conf: Configuration = Query(r.db).configuration
    val assigned: Boolean = Query(r.db) assignedTo (r.user.id) map (_.id) contains paperId
    val notReviewed: Boolean = Query(r.db).reviewOf(r.user.id, paperId).isEmpty
    if(conf.pcmemberReview && assigned && notReviewed)
      Ok(views.html.review("Submission " + Query(r.db).indexOf(paperId), reviewForm, Query(r.db).paperWithId(paperId), Navbar(PC_Member))(Submitting.summaryImpl(paperId)))
    else if(conf.pcmemberComment)
      doCommentImpl(paperId, routes.Reviewing.doComment(paperId), Navbar(PC_Member))
    else
      Submitting.infoImpl(paperId, None, None, Navbar(PC_Member))
  }
  
  def doReview(paperId: Id[Paper]) = SlickAction(NonConflictingPCMember(paperId), _.pcmemberReview) { 
    implicit r =>
    reviewForm.bindFromRequest.fold(
      errors => {
        // review(id, errors)(r), // TODO: DRY with this, use Action.async everywhere...
        val paper: Paper = Query(r.db) paperWithId paperId
        Ok(views.html.review("Submission " + Query(r.db).indexOf(paperId), errors, paper, Navbar(PC_Member))(Submitting.summaryImpl(paperId)))
      },
      review => {
        r.connection insert review.copy(paperId=paperId, personId=r.user.id)
        Redirect(routes.Reviewing.review(paperId))
      }
    )
  }
  
  def doCommentImpl(paperId: Id[Paper], doCommentEP: Call, navbar: Html)(implicit r: SlickRequest[_]) = {
    val optionalReview: Option[Review] = Query(r.db) reviewOf (r.user.id, paperId)
    def canEdit(review: Review): Boolean = optionalReview map (_ == review) getOrElse false
    val comments = Query(r.db) commentsOn paperId map Left.apply
    val reviews = Query(r.db) reviewsHistoryOn paperId map Right.apply
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering fromLessThan (_ isAfter _)
    val commentReviews: List[Either[Comment, Review]] = (comments ::: reviews).sortBy(
      _ fold (_.updatedAt, _.updatedAt)).reverse
    Ok(views.html.comment("Submission " + Query(r.db).indexOf(paperId), commentReviews, Query(r.db).allPersons.toSet, canEdit, doCommentEP, navbar)(Submitting.summaryImpl(paperId)))
  }
  
  def doComment(paperId: Id[Paper]) = SlickAction(NonConflictingPCMember(paperId), _.pcmemberComment) { 
    implicit r =>
    commentForm.bindFromRequest.fold(_ => (),
      comment => r.connection insert comment.copy(paperId=paperId, personId=r.user.id))
    Redirect(routes.Reviewing.review(paperId))
  }

  def editReview(paperId: Id[Paper], personId: Id[Person]) = SlickAction(NonConflictingPCMember(paperId), _.pcmemberReview) { implicit r =>
    reviewForm.bindFromRequest.fold(_ => (),
      review => {
        r.connection insert review.copy(paperId=paperId, personId=personId)
      }
    )
    Redirect(routes.Reviewing.review(paperId))
  }
}
