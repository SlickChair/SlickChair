package controllers.member

import controllers.MemberOrChair
import models.entities.{Members, Papers, Topics}
import models.relations.{MemberBid, MemberBids, MemberTopic, MemberTopics}
import models.relations.Bid
import models.relations.Bid.Bid
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, nonEmptyText, number}
import play.api.data.Mapping
import play.api.mvc.Controller
import securesocial.core.SecureSocial

case class BidForm(
  bids: List[MemberBid],
  topics: List[Int]
)

object Bidding extends Controller with SecureSocial {
  val bidMapping: Mapping[Bid] = mapping(
    "value" -> nonEmptyText)(Bid.withName(_))(Some(_).map(_.toString))
  
  val memberBidMapping: Mapping[MemberBid] = mapping(
    "paperid" -> number,
    "memberid" -> ignored(null.asInstanceOf[Int]),
    "bid" -> bidMapping
  )(MemberBid.apply _)(MemberBid.unapply _)

  val bidForm: Form[BidForm] = Form(
    mapping(
      "memberbids" -> list(memberBidMapping),
      "topics" -> list(number)
    )(BidForm.apply _)(BidForm.unapply _)
  )
  
  def form = SecuredAction(MemberOrChair) { implicit request =>
    val member = Members.getFromRequest
    val memberBids = MemberBids.of(member)
    val existingBidForm = bidForm.bind(
      Topics.of(member).map(topic => ("topics[%s]".format(topic.id), topic.id.toString)).toMap
      ++ memberBids.map(mb => ("memberbids[%s].bid.value".format(mb.paperid), mb.bid.toString)).toMap
      ++ Papers.all.filterNot(paper => memberBids.map(_.paperid).contains(paper.id))
        .map(paper => ("memberbids[%s].bid.value".format(paper.id), Bid.Medium.toString)).toMap
    )
    Ok(views.html.member.bid(None, existingBidForm))
  }
  
  def make = SecuredAction(MemberOrChair) { implicit request =>
    val member = Members.getFromRequest
    val bindedForm = bidForm.bindFromRequest
    bindedForm.fold(
      _ => Ok(views.html.member.bid(Some("Error found!"), bindedForm)),
      { case f @ BidForm(memberbids, topicIds) =>
        MemberTopics.deleteFor(member)
        MemberTopics.insertAll(topicIds.map(MemberTopic(member.id, _)))
        MemberBids.deleteFor(member)
        MemberBids.insertAll(memberbids.map(_.copy(memberid = member.id)))
        Ok(views.html.member.bid(Some("Saved"), bindedForm))
      }
    )    
  }
}
