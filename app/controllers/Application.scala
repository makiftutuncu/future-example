package controllers

import play.api.mvc._

object Application extends Controller
{
  /**
   * Main action for rendering the page
   */
  def get = Action {
    Ok(views.html.index())
  }
}