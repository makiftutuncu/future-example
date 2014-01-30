package controllers

import play.api.mvc._

object Upload extends Controller
{
  /**
   * Main action for rendering the page
   */
  def get = Action {
    Ok(views.html.upload())
  }

  /**
   * Upload action that reads the file from user selection and runs uploading task
   */
  def upload = TODO
}