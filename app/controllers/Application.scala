package controllers

import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.io._
import play.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.ws.WS
import play.api.libs.iteratee._

case class DownloadUrl(url: String)

object Application extends Controller
{
  def isValidUrl(url: String) =
  {
    ((url startsWith "http://") || (url startsWith "https://")) && (
    (url endsWith ".jpg") || (url endsWith ".jpeg") || (url endsWith ".JPG") || (url endsWith ".JPEG") ||
    (url endsWith ".png") || (url endsWith ".PNG") ||
    (url endsWith ".gif") || (url endsWith ".GIF"))
  }

  val downloadForm: Form[DownloadUrl] = Form(
    mapping("url" -> nonEmptyText.verifying("Invalid image URL!", isValidUrl(_)))
    (DownloadUrl.apply)(DownloadUrl.unapply)
  )

  val path: String = """public/images/"""
  val fileName: String = """image.jpg"""

  def index = Action {
    val file = new File(path + fileName)
    if(file.exists)
      Ok(views.html.index(file.getName))
    else
      Ok(views.html.index(""))
  }

  def fromStream(stream: OutputStream): Iteratee[Array[Byte], Unit] = Cont {
    case e @ Input.EOF =>
      stream.close()
      Logger.debug("Successful!")
      Redirect(routes.Application.index)
      Done((), e)

    case Input.El(data) =>
      stream.write(data)
      Logger.debug(s"Downloaded ${data.length} bytes.")
      fromStream(stream)

    case Input.Empty =>
      fromStream(stream)
  }

  def download = Action.async {
    implicit request => downloadForm.bindFromRequest.fold(
      errors => {
        Logger.error("Cannot download: " + errors.errorsAsJson)
        Future.successful {
          Redirect(routes.Application.index)
        }
      },
      downloadUrl => {
        Logger.debug("Starting to download...")
        WS.url(downloadUrl.url).withRequestTimeout(10000).get map {
        response =>
            try
            {
              val out = new FileOutputStream(path + fileName)
              out.write(response.getAHCResponse.getResponseBodyAsBytes)
              out.close()
              Logger.debug("Successful!")
              Redirect(routes.Application.index)
            }
            catch
            {
              case _ => throw new Exception("IO exception!")
            }
        } recover {
          case _ =>
            Logger.debug("Failed!")
            new File(path + fileName).delete()
            Redirect(routes.Application.index)
        }
      }
    )
  }
}