package controllers

import java.lang.Math.{log, pow}
import models.{File, Id, Query}
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Controller, ResponseHeader, SimpleResult}
import play.api.templates.Html

object FileServing extends Controller {
  def apply(fileId: Id[File]) = SlickAction(AuthorOrNCPCMember(fileId), _ => true) {
      implicit r =>
    val file = Query(r.db) fileWithId fileId
    val headers =
      if(file.name endsWith ".pdf") Map(
        CONTENT_TYPE -> "application/pdf",
        CONTENT_DISPOSITION -> s"inline; filename=${file.name}")
      else Map(
        CONTENT_TYPE -> "application/octet-stream",
        CONTENT_DISPOSITION -> s"attachment; filename=${file.name}")
    SimpleResult(
      header=ResponseHeader(200, headers),
      body=Enumerator(file.content)
    )
  }

  def linkToFile(file: File): Html = {
    /** Source: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java */
    def humanReadableByteCount(size: Long): String = {
      import Math._
      val unit = 1000
      if (size < unit)
         size + " B"
      else {
        val exp = (log(size) / log(unit)).toInt
        val pre = "kMGTPE" charAt (exp - 1)
        "%.1f %sB" format (size / pow(unit, exp), pre)
      }
    }
    Html(s"""
    <a href="${routes.FileServing(file.id)}">
    ${file.name} (${humanReadableByteCount(file.size)})</a>""")
  }
}
