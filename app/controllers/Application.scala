package controllers

import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util._
import sys.process._
import java.net.URL
import java.io.File
import play.Logger

object Application extends Controller
{
  val fileUrl: String = """https://dl.dropboxusercontent.com/u/37485576/Turkish%20Flag.jpg"""
  val fileName: String = """flag.jpg"""

  def index = Action
  {
    val f = Future
    {
      Logger.debug("Starting to download...")

      val url = new URL(fileUrl)
      val file = new File("public/images/" + fileName)
      url #> file !!
    }

    f map
    {
      result => Logger.debug("Successful!")
    } recover
    {
      case _ => Logger.debug("Failed!"); Future { new File("public/images/" + fileName).delete() }
    }

    Ok("Image download with Scala Future")
  }
}