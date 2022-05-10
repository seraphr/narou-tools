package jp.seraphr.narou

import narou4j.Narou
import narou4j.entities.NovelBody
import narou4j.network.NarouApiClient
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.{ Document, Element }

/**
 */
class MyNarou extends Narou {
  override def getNovelBody(ncode: String, page: Int): NovelBody = {
    if (page <= 0) throw new RuntimeException("pageは1以上である必要があります")
    val client = new NarouApiClient

    val response: Response = client.getNovelBody(ncode, page)
    val html: String       = response.body.string
    if (html.isEmpty) {
      throw new RuntimeException("empty body")
    }

    val result: NovelBody  = new NovelBody
    result.setNcode(ncode)
    result.setPage(page)
    val document: Document = Jsoup.parse(html)
    val title: String      = document.select(".novel_subtitle").first.ownText
    result.setTitle(title)
    val element: Element   = document.getElementById("novel_honbun")
    var body: String       = element.html

    //    if(page <= 3)
    //      FileUtils.write(new File(s"./orig_${ncode}_${page}"), body, "UTF-8")

    body = body.replaceAll("\n", "")
    body = body.replaceAll("\r", "")
    body = body
      .replaceAll("<ruby>", "")
      .replaceAll("</ruby>", "")
      .replaceAll("<rb>", "")
      .replaceAll("</rb>", "")
      .replaceAll("<rt>", "")
      .replaceAll("</rt>", "")
      .replaceAll("<rp>", "")
      .replaceAll("</rp>", "")
    body = body.replaceAll("<br>", "\n")
    result.setBody(body)
    return result
  }

}
