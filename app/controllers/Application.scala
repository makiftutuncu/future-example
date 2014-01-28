package controllers

import play.api.mvc._

object Application extends Controller
{
  val url: String = "http://www.foo.bar/test.zip"

  def index = Action
  {
    Ok(views.html.index(url))
  }
}