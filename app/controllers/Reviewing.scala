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
import Utils._

case class BidForm(bids: List[Bid])

object Reviewing extends Controller {
  val bidMapping: Mapping[Bid] = mapping(
    "metadata" -> curse[MetaData[Bid]],
    "paperid" -> idMapping[Paper],
    "personid" -> curse[Id[Person]],
    "bid" -> enumMapping(BidValue)
  )(Bid.apply _)(Bid.unapply _)

  val bidForm: Form[BidForm] = Form(
    mapping("bids" -> list(bidMapping))
    (BidForm.apply _)(BidForm.unapply _)
  )

  def bid() = SlickAction(IsReviewer) { implicit r =>
    val bids: List[Bid] = Bids.of(r.user.id)
    val papers: List[Paper] = Papers.all
    val allBids: List[Bid] = papers map { p =>
      bids.find(_.paperid == p.id) match {
        case None => Bid(newMetaData(), p.id, r.user.id, Maybe)
        case Some(b) => b
      }
    }
    val form = bidForm fill BidForm(allBids)
    Ok(views.html.bid(form, papers.toSet, Files.all.toSet, Navbar(Reviewer)))
  }

  def doBid() = SlickAction(IsReviewer) { implicit r =>
    bidForm.bindFromRequest.fold(
      errors => 
        Ok(views.html.bid(errors, Papers.all.toSet, Files.all.toSet, Navbar(Reviewer))),
      form => {
        val bids = form.bids map { _ copy (metadata=newMetaData(), personid=r.user.id) }
        Bids insAll bids
        Redirect(routes.Reviewing.bid)
      }
    )
  }

  def papers() = SlickAction(IsReviewer) { implicit r =>
    Ok(views.html.main("List of all submissions", Navbar(Reviewer))(Html(
      Papers.all.toString.replaceAll(",", ",\n<br>"))))
  }
  
  def make(id: IdType) = SlickAction(NonConflictingReviewer(id)) { implicit r =>
    val paper: Paper = Papers.withId(Id[Paper](id))
    Ok(views.html.main("Submission " + shorten(paper.id.value), Navbar(Reviewer)) (
       views.html.review(paper, Authors.of(paper.id), Topics.of(paper.id), paper.fileid.map(Files withId _))
    ))
  }
  
  def doMake(id: IdType) = SlickAction(NonConflictingReviewer(id)) { implicit r =>
    Ok("")
  }
}
