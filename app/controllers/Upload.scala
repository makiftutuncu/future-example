package controllers

import play.api.mvc._
import java.io._
import play.Logger

object Upload extends Controller
{
  /**
   * Path to which the image will be saved
   */
  private val path: String = """public/images/"""

  /**
   * Name of the file to save the image
   */
  private val fileName: String = """uploaded.jpg"""

  /**
   * Main action for rendering the page
   */
  def get = Action {
    val file = new File(path + fileName)
    if(file.exists)
      Ok(views.html.upload(file.getName))
    else
      Ok(views.html.upload(""))
  }

  /**
   * Upload action that reads the file from user selection and runs uploading task
   */
  def upload = Action(parse.multipartFormData) {
    implicit request => request.body.file("file").map {
      picture =>
        picture.contentType match {
          case Some(contentType: String) =>
            if(contentType == "image/jpeg")
            {
              Logger.debug(s"Uploading ${picture.filename}...")
              picture.ref.moveTo(new File(path + fileName))
              Logger.debug("Successful!")
            }
            else Logger.debug("Failed! Only jpg images are supported.")

          case _ => Logger.debug("Failed! Only jpg images are supported.")
        }
        Redirect(routes.Upload.get())
    }.getOrElse {
      Logger.debug("Failed!")
      Redirect(routes.Upload.get())
    }
  }
}