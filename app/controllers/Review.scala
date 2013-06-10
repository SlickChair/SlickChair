package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._
import securesocial.core.SecureSocial

object Review extends Controller with SecureSocial {
  def list = TODO
  def view(id: Int) = TODO
  def make(id: Int) = TODO
  def edit(id: Int) = TODO
  def makeComment(id: Int) = TODO
  def editComment(sid: Int, cid: Int) = TODO
}