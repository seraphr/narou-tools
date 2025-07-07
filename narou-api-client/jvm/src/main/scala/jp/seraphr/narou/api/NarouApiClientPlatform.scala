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

  private def decompressGzip(data: Array[Byte]): Array[Byte] = {
    new GZIPInputStream(new ByteArrayInputStream(data)).readAllBytes()
  }

  /** JVM用のクライアントを作成する */
  def create(): Task[NarouApiClient] = {
    HttpClientMonixBackend().map(backend => new NarouApiClientJVM(backend, gzipDecoder = Some(decompressGzip)))
  }

  /** JVM用の具象実装 */
  private class NarouApiClientJVM(backend: sttp.client4.Backend[Task], gzipDecoder: Option[Array[Byte] => Array[Byte]])
      extends NarouApiClientImpl(backend, gzipDecoder) {

    override protected def parseNovelTablePlatform(html: String, ncode: String): List[NovelBody] = {
      if (html.isEmpty) {
        List.empty
      } else {
        val document = Jsoup.parse(html)
        val indexBox = document.select(".index_box").first()
        if (indexBox != null) {
          val elements = indexBox.children()
          val result   = scala.collection.mutable.ListBuffer[NovelBody]()

          for (element <- elements.asScala) {
            element.className() match {
              case "chapter_title"  =>
                result += NovelBody(
                  ncode = ncode,
                  title = element.ownText(),
                  isChapter = true
                )
              case "novel_sublist2" =>
                val linkElement = element.select(".subtitle a").first()
                if (linkElement != null) {
                  val href  = linkElement.attr("href")
                  val parts = href.split("/")
                  if (parts.length >= 3) {
                    val pageNumber =
                      try { parts(2).toInt }
                      catch { case _: NumberFormatException => 0 }
                    if (pageNumber > 0) {
                      result += NovelBody(
                        ncode = ncode,
                        page = pageNumber,
                        title = linkElement.ownText(),
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
        val document = Jsoup.parse(html)

        // タイトル取得
        val titleElement = document.select(".novel_subtitle").first()
        val title        = if (titleElement != null) titleElement.ownText() else ""

        // 本文取得
        val bodyElement = document.getElementById("novel_honbun")
        val body        = if (bodyElement != null) {
          val rawBody       = bodyElement.html()
          val lineSeparator = System.getProperty("line.separator")
          rawBody
            .replaceAll(lineSeparator, "")
            .replaceAll("<ruby>", "")
            .replaceAll("</ruby>", "")
            .replaceAll("<rb>", "")
            .replaceAll("</rb>", "")
            .replaceAll("<rt>", "")
            .replaceAll("</rt>", "")
            .replaceAll("<rp>", "")
            .replaceAll("</rp>", "")
            .replaceAll("<br>", lineSeparator)
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
