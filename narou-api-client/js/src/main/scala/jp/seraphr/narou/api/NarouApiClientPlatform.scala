package jp.seraphr.narou.api

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

import jp.seraphr.narou.api.model.NovelBody

import monix.eval.Task
import sttp.client4.impl.monix.FetchMonixBackend

/** JS用のNarouApiClient実装 */
object NarouApiClientPlatform {

  /** JS用のクライアントを作成する */
  def create(): Task[NarouApiClient] = {
    Task.now(new NarouApiClientJS(FetchMonixBackend(), gzipDecoder = None))
  }

  /** JS用の具象実装 */
  private class NarouApiClientJS(backend: sttp.client4.Backend[Task], gzipDecoder: Option[Array[Byte] => Array[Byte]])
      extends NarouApiClientImpl(backend, gzipDecoder) {

    override protected def parseNovelTablePlatform(html: String, ncode: String): List[NovelBody] = {
      if (html.isEmpty) {
        List.empty
      } else {
        val parser   = new dom.DOMParser()
        val doc      = parser.parseFromString(html, org.scalajs.dom.MIMEType.`text/html`)
        val indexBox = doc.querySelector(".index_box")
        if (indexBox != null) {
          val elements = indexBox.children
          val result   = scala.collection.mutable.ListBuffer[NovelBody]()

          for (i <- 0 until elements.length) {
            val element = elements(i)
            element.asInstanceOf[org.scalajs.dom.html.Element].className match {
              case "chapter_title"  =>
                result += NovelBody(
                  ncode = ncode,
                  title = element.textContent,
                  isChapter = true
                )
              case "novel_sublist2" =>
                val linkElement = element.querySelector(".subtitle a")
                if (linkElement != null) {
                  val href  = linkElement.getAttribute("href")
                  val parts = href.split("/")
                  if (parts.length >= 3) {
                    val pageNumber =
                      try { parts(2).toInt }
                      catch { case _: NumberFormatException => 0 }
                    if (pageNumber > 0) {
                      result += NovelBody(
                        ncode = ncode,
                        page = pageNumber,
                        title = linkElement.textContent,
                        isChapter = false
                      )
                    }
                  }
                }
              case _                => // その他の要素は無視
            }
          }
          result.toList
        } else {
          List.empty
        }
      }
    }

    override protected def parseNovelBodyPlatform(html: String, ncode: String, page: Int): NovelBody = {
      if (html.isEmpty) {
        NovelBody(ncode = ncode, page = page)
      } else {
        val parser = new dom.DOMParser()
        val doc    = parser.parseFromString(html, org.scalajs.dom.MIMEType.`text/html`)

        // タイトル取得
        val titleElement = doc.querySelector(".novel_subtitle")
        val title        = if (titleElement != null) titleElement.textContent else ""

        // 本文取得
        val bodyElement = doc.getElementById("novel_honbun")
        val body        = if (bodyElement != null) {
          val rawBody = bodyElement.innerHTML
          rawBody
            .replaceAll("<ruby>", "")
            .replaceAll("</ruby>", "")
            .replaceAll("<rb>", "")
            .replaceAll("</rb>", "")
            .replaceAll("<rt>", "")
            .replaceAll("</rt>", "")
            .replaceAll("<rp>", "")
            .replaceAll("</rp>", "")
            .replaceAll("<br>", "\n")
        } else ""

        NovelBody(
          ncode = ncode,
          page = page,
          title = title,
          body = body,
          isChapter = false
        )
      }
    }

  }

}
