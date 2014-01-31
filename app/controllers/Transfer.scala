package controllers

import play.api.mvc._
import java.io._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee._
import play.api.libs.ws.{Response, WS}
import scala.concurrent.Future

object Transfer extends Controller
{
  /**
   * Path to which the image will be read
   */
  private val path: String = """public/images/"""

  /**
   * Name of the file from which to read the image
   */
  private val fileName: String = """transferred.jpg"""

  /**
   * Main action for rendering the page
   */
  def get = Action {
    Ok(views.html.transfer())
  }

  /**
   * Transfer action that reads the file and runs transferring task
   */
  def transfer = Action.async {
    val file = new File(path + fileName)
    val response: Future[Response] = WS.url("http://localhost:9000/downloadInternal").withRequestTimeout(10000).post(file)

    response map {
      r => Ok(r.body)
    } recover {
      case e: Exception => Ok("Error " + e.getMessage)
    }
  }
}