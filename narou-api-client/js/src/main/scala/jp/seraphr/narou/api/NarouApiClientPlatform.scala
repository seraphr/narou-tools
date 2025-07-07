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
    Task.now(new NarouApiClientJS(FetchMonixBackend(), aGzipDecoder = None))
  }

  /** JS用の具象実装 */
  private class NarouApiClientJS(aBackend: sttp.client4.Backend[Task], aGzipDecoder: Option[Array[Byte] => Array[Byte]])
      extends NarouApiClientImpl(aBackend, aGzipDecoder) {

    override protected def parseNovelTablePlatform(aHtml: String, aNcode: String): List[NovelBody] = {
      if (aHtml.isEmpty) {
        List.empty
      } else {
        val tParser   = new dom.DOMParser()
        val tDoc      = tParser.parseFromString(aHtml, org.scalajs.dom.MIMEType.`text/html`)
        val tIndexBox = tDoc.querySelector(".index_box")
        if (tIndexBox != null) {
          val tElements = tIndexBox.children

          (0 until tElements.length)
            .foldLeft(List.empty[NovelBody]) { (tAcc, tIndex) =>
              val tElement = tElements(tIndex)
              tElement.asInstanceOf[org.scalajs.dom.html.Element].className match {
                case "chapter_title"  =>
                  NovelBody(
                    ncode = aNcode,
                    title = tElement.textContent,
                    isChapter = true
                  ) :: tAcc
                case "novel_sublist2" =>
                  val tLinkElement = tElement.querySelector(".subtitle a")
                  if (tLinkElement != null) {
                    val tHref  = tLinkElement.getAttribute("href")
                    val tParts = tHref.split("/")
                    if (tParts.length >= 3) {
                      val tPageNumber =
                        try { tParts(2).toInt }
                        catch { case _: NumberFormatException => 0 }
                      if (tPageNumber > 0) {
                        NovelBody(
                          ncode = aNcode,
                          page = tPageNumber,
                          title = tLinkElement.textContent,
                          isChapter = false
                        ) :: tAcc
                      } else tAcc
                    } else tAcc
                  } else tAcc
                case _                => tAcc // その他の要素は無視
              }
            }
            .reverse
        } else {
          List.empty
        }
      }
    }

    override protected def parseNovelBodyPlatform(aHtml: String, aNcode: String, aPage: Int): NovelBody = {
      if (aHtml.isEmpty) {
        NovelBody(ncode = aNcode, page = aPage)
      } else {
        val tParser = new dom.DOMParser()
        val tDoc    = tParser.parseFromString(aHtml, org.scalajs.dom.MIMEType.`text/html`)

        // タイトル取得
        val tTitleElement = tDoc.querySelector(".novel_subtitle")
        val tTitle        = if (tTitleElement != null) tTitleElement.textContent else ""

        // 本文取得
        val tBodyElement = tDoc.getElementById("novel_honbun")
        val tBody        = if (tBodyElement != null) {
          val tRawBody = tBodyElement.innerHTML
          tRawBody
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
          ncode = aNcode,
          page = aPage,
          title = tTitle,
          body = tBody,
          isChapter = false
        )
      }
    }

  }

}
