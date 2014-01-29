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

/**
 * A tiny and utterly useless class for retrieving URL provided by the user
 */
case class DownloadUrl(url: String)

object Application extends Controller
{
  /**
   * A simple URL validation method for image links
   */
  def isValidUrl(url: String) =
  {
    ((url startsWith "http://") || (url startsWith "https://")) &&
    ((url endsWith ".jpg") || (url endsWith ".jpeg") || (url endsWith ".JPG") || (url endsWith ".JPEG"))
  }

  /**
   * A Play Form for retrieving the URL provided by the user
   */
  val downloadForm: Form[DownloadUrl] = Form(
    mapping("url" -> nonEmptyText.verifying("Invalid image URL!", isValidUrl(_)))
    (DownloadUrl.apply)(DownloadUrl.unapply)
  )

  /**
   * Path to which the image will be downloaded
   */
  private val path: String = """public/images/"""

  /**
   * Name of the file to save the image
   */
  private val fileName: String = """image.jpg"""

  /**
   * Main action for rendering the page
   */
  def index = Action {
    val file = new File(path + fileName)
    if(file.exists)
      Ok(views.html.index(file.getName))
    else
      Ok(views.html.index(""))
  }

  /**
   * Iteratee method to process stream
   */
  def fromStream(stream: OutputStream): Iteratee[Array[Byte], SimpleResult] = Cont {
    case Input.EOF =>
      stream.close()
      Logger.debug("Successful!")
      Done(Redirect(routes.Application.index()))

    case Input.El(data) =>
      stream.write(data)
      Logger.debug(s"Downloaded ${data.length} bytes.")
      fromStream(stream)

    case Input.Empty =>
      Logger.debug("Received empty input.")
      fromStream(stream)
  }

  /**
   * Download action that reads the URL from user input and runs downloading task
   */
  def download = Action.async {
    implicit request => downloadForm.bindFromRequest.fold(
      errors => {
        Logger.error("Cannot download: " + errors.errorsAsJson)
        Future.successful {
          Redirect(routes.Application.index())
        }
      },
      downloadUrl => {
        Logger.debug("Starting to download...")

        // Start a GET request with 10 seconds timeout
        WS.url(downloadUrl.url).withRequestTimeout(10000).get {
          // Write the request to a file
          headers => fromStream(new BufferedOutputStream(new FileOutputStream(path + fileName)))
        } flatMap {
          // Run the iteration for downloading process
          _.run
        } recover {
          case e: Exception =>
            Logger.debug("Failed! " + e.getMessage)
            new File(path + fileName).delete()
            Redirect(routes.Application.index())
        }
      }
    )
  }
}