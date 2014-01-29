package controllers

import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util._
import sys.process._
import java.net.URL
import java.io.File
import play.Logger
import play.api.data.Form
import play.api.data.Forms._

case class DownloadUrl(url: String)

object Application extends Controller
{
  def isValidUrl(url: String) =
  {
    ((url startsWith "http://") || (url startsWith "https://")) && (
    (url endsWith ".jpg") ||
    (url endsWith ".jpeg") ||
    (url endsWith ".JPG") ||
    (url endsWith ".JPEG") ||
    (url endsWith ".png") ||
    (url endsWith ".PNG") ||
    (url endsWith ".gif") ||
    (url endsWith ".GIF"))
  }

  val downloadForm: Form[DownloadUrl] = Form(
    mapping("url" -> nonEmptyText.verifying("Invalid image URL!", isValidUrl(_)))
    (DownloadUrl.apply)(DownloadUrl.unapply)
  )

  val fileName: String = """image.jpg"""

  def index = Action
  {
    val file = new File("public/images/" + fileName)
    if(file.exists)
      Ok(views.html.index(file.getName))
    else
      Ok(views.html.index(""))
  }

  def download = Action.async
  {
    implicit request => downloadForm.bindFromRequest.fold(
      errors => {
        Logger.error("Cannot download: " + errors.errorsAsJson)
        Future.successful {
          Redirect(routes.Application.index)
        }
      },
      downloadUrl => {
        val f = Future
        {
          Logger.debug("Starting to download...")

          val url = new URL(downloadUrl.url)
          val file = new File("public/images/" + fileName)
          url #> file !!
        }

        f map {
          result => {
            Logger.debug("Successful!")
            Redirect(routes.Application.index())
          }
        } recover {
          case _ => {
            Logger.debug("Failed!")
            Future {
              new File("public/images/" + fileName).delete()
            }
            Redirect(routes.Application.index())
          }
        }
      }
    )
  }
}