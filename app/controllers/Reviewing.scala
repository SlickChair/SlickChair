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
        case None => Bid((newId[Bid](), r.now, r.user.email), p.id, r.user.id, Maybe)
        case Some(b) => b
      }
    }
    val form = bidForm fill BidForm(allBids)
    Ok(views.html.member.bid(form, papers.toSet, Files.all.toSet, Navbar(Reviewer)))
  }

  def dobid() = SlickAction(IsReviewer) { implicit r =>
    bidForm.bindFromRequest.fold(
      errors => 
       Ok(views.html.main("dobid errors", Navbar(Reviewer))(Html(errors.toString))),
      form =>
       Ok(views.html.main("TODO", Navbar(Reviewer))(Html(form.toString)))
    )
  }

  def papers() = SlickAction(IsReviewer) { implicit r =>
    Ok(views.html.main("List of all submissions", Navbar(Reviewer))(Html(
      Papers.all.toString.replaceAll(",", ",\n<br>"))))
  }
}
