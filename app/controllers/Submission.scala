package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models._
import securesocial.core.SecureSocial

object Submission extends Controller with SecureSocial {
  def form = TODO
  def make = TODO
  def info(id: Int) = TODO
  def edit(id: Int) = TODO
}