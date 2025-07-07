package jp.seraphr.narou.api

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

import scala.jdk.CollectionConverters._

import jp.seraphr.narou.api.model.NovelBody

import monix.eval.Task
import org.jsoup.Jsoup
import sttp.client4.httpclient.monix.HttpClientMonixBackend

/** JVM用のNarouApiClient実装 */
object NarouApiClientPlatform {

  private def decompressGzip(aData: Array[Byte]): Array[Byte] = {
    new GZIPInputStream(new ByteArrayInputStream(aData)).readAllBytes()
  }

  /** JVM用のクライアントを作成する */
  def create(): Task[NarouApiClient] = {
    HttpClientMonixBackend().map(aBackend => new NarouApiClientJVM(aBackend, aGzipDecoder = Some(decompressGzip)))
  }

  /** JVM用の具象実装 */
  private class NarouApiClientJVM(aBackend: sttp.client4.Backend[Task], aGzipDecoder: Option[Array[Byte] => Array[Byte]])
      extends NarouApiClientImpl(aBackend, aGzipDecoder) {

    override protected def parseNovelTablePlatform(aHtml: String, aNcode: String): List[NovelBody] = {
      if (aHtml.isEmpty) {
        List.empty
      } else {
        val tDocument = Jsoup.parse(aHtml)
        val tIndexBox = tDocument.select(".index_box").first()
        if (tIndexBox != null) {
          val tElements = tIndexBox.children()

          tElements
            .asScala
            .foldLeft(List.empty[NovelBody]) { (tAcc, tElement) =>
              tElement.className() match {
                case "chapter_title"  =>
                  tAcc :+ NovelBody(
                    ncode = aNcode,
                    title = tElement.ownText(),
                    isChapter = true
                  )
                case "novel_sublist2" =>
                  val tLinkElement = tElement.select(".subtitle a").first()
                  if (tLinkElement != null) {
                    val tHref  = tLinkElement.attr("href")
                    val tParts = tHref.split("/")
                    if (tParts.length >= 3) {
                      val tPageNumber =
                        try { tParts(2).toInt }
                        catch { case _: NumberFormatException => 0 }
                      if (tPageNumber > 0) {
                        tAcc :+ NovelBody(
                          ncode = aNcode,
                          page = tPageNumber,
                          title = tLinkElement.ownText(),
                          isChapter = false
                        )
                      } else tAcc
                    } else tAcc
                  } else tAcc
                case _                => tAcc // その他の要素は無視
              }
            }
        } else {
          List.empty
        }
      }
    }

    override protected def parseNovelBodyPlatform(aHtml: String, aNcode: String, aPage: Int): NovelBody = {
      if (aHtml.isEmpty) {
        NovelBody(ncode = aNcode, page = aPage)
      } else {
        val tDocument = Jsoup.parse(aHtml)

        // タイトル取得
        val tTitleElement = tDocument.select(".novel_subtitle").first()
        val tTitle        = if (tTitleElement != null) tTitleElement.ownText() else ""

        // 本文取得
        val tBodyElement = tDocument.getElementById("novel_honbun")
        val tBody        = if (tBodyElement != null) {
          val tRawBody       = tBodyElement.html()
          val tLineSeparator = System.getProperty("line.separator")
          tRawBody
            .replaceAll(tLineSeparator, "")
            .replaceAll("<ruby>", "")
            .replaceAll("</ruby>", "")
            .replaceAll("<rb>", "")
            .replaceAll("</rb>", "")
            .replaceAll("<rt>", "")
            .replaceAll("</rt>", "")
            .replaceAll("<rp>", "")
            .replaceAll("</rp>", "")
            .replaceAll("<br>", tLineSeparator)
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
