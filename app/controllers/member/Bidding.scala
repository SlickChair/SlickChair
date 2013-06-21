package controllers.member

import org.joda.time.DateTime
import models.secureSocial._
import models.entities._
import models.relations.{Comment, Comments, NewComment, Review, ReviewConfidence}
import models.relations.ReviewConfidence.ReviewConfidence
import models.relations.ReviewEvaluation
import models.relations.ReviewEvaluation.ReviewEvaluation
import models.relations.Reviews
import play.api.Play.current
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import securesocial.core.{SecureSocial, SecuredRequest}
import controllers._
import models.relations._
import models.relations.Bid._


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
    Ok(views.html.bid(None, existingBidForm))
  }
  
  def make = SecuredAction(MemberOrChair) { implicit request =>
    val member = Members.getFromRequest
    val bindedForm = bidForm.bindFromRequest
    bindedForm.fold(
      _ => Ok(views.html.bid(Some("Error found!"), bindedForm)),
      { case f @ BidForm(memberbids, topicIds) =>
        MemberTopics.deleteFor(member)
        MemberTopics.insertAll(topicIds.map(MemberTopic(member.id, _)))
        MemberBids.deleteFor(member)
        MemberBids.insertAll(memberbids.map(_.copy(memberid = member.id)))
        Ok(views.html.bid(Some("Saved"), bindedForm))
      }
    )    
  }
}