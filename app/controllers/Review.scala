package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._
import securesocial.core.SecureSocial
import models.entities._

object Review extends Controller with SecureSocial {
  def list = Action(
    Ok(views.html.paperList(
      Papers.all.map(p => (p, Authors.of(p)))
    ))
  )
  
  def info(id: Int) = TODO
  def makeReview(id: Int) = TODO
  def editReview(id: Int) = TODO
  def makeComment(id: Int) = TODO
  def editComment(sid: Int, cid: Int) = TODO
}