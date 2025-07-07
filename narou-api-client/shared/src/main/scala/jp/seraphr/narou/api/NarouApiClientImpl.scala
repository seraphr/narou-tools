package jp.seraphr.narou.api

import scala.concurrent.duration._

import jp.seraphr.narou.api.model.{ NovelApiResponse, NovelBody, SearchParams }
import jp.seraphr.narou.api.model.given

import monix.eval.Task
import sttp.client4._
import sttp.client4.circe.{ asJson, deserializeJson }

/** なろう小説APIクライアントの実装 */
abstract class NarouApiClientImpl(backend: Backend[Task], gzipDecoder: Option[Array[Byte] => Array[Byte]])
    extends NarouApiClient {

  private val baseUrl    = "https://api.syosetu.com/novelapi/api/"
  private val mGzipQuery = Option.when(gzipDecoder.isDefined)("gzip=5")

  override def search(aParams: SearchParams): Task[NovelApiResponse] = {
    val tQueryParams = buildQueryParams(aParams)
    val tFullUrl     = s"$baseUrl?$tQueryParams"

    val tAsJson  = gzipDecoder match {
      case Some(d) =>
        asByteArray.mapRight((ba: Array[Byte]) => new String(d(ba))).map(_.flatMap(deserializeJson[NovelApiResponse]))
      case None    => asJson[NovelApiResponse]
    }
    val tRequest = basicRequest.get(uri"$tFullUrl").response(tAsJson).readTimeout(30.seconds)

    backend
      .send(tRequest)
      .map { tResponse =>
        tResponse.body match {
          case Right(tApiResponse)     => tApiResponse
          case Left(tError: Throwable) => throw new RuntimeException(s"API request failed", tError)
          case Left(tError)            => throw new RuntimeException(s"API request failed: $tError")
        }
      }
  }

  private def buildQueryParams(aParams: SearchParams): String = {
    val tBaseQueryMap = Map(
      "word"        -> aParams.word,
      "notword"     -> aParams.notword,
      "biggenre"    -> aParams.biggenre.map(_.id.toString),
      "notbiggenre" -> aParams.notbiggenre.map(_.id.toString),
      "genre"       -> aParams.genre.map(_.id.toString),
      "notgenre"    -> aParams.notgenre.map(_.id.toString),
      "userid"      -> aParams.userid.map(_.toString),
      "ncode"       -> aParams.ncode,
      "title"       -> aParams.title,
      "ex"          -> aParams.ex,
      "keyword"     -> aParams.keyword,
      "wname"       -> aParams.wname,
      "isr15"       -> aParams.isr15.map(if (_) "1" else "0"),
      "isbl"        -> aParams.isbl.map(if (_) "1" else "0"),
      "isgl"        -> aParams.isgl.map(if (_) "1" else "0"),
      "iszankoku"   -> aParams.iszankoku.map(if (_) "1" else "0"),
      "istensei"    -> aParams.istensei.map(if (_) "1" else "0"),
      "istenni"     -> aParams.istenni.map(if (_) "1" else "0"),
      "notr15"      -> aParams.notr15.map(if (_) "1" else "0"),
      "notbl"       -> aParams.notbl.map(if (_) "1" else "0"),
      "notgl"       -> aParams.notgl.map(if (_) "1" else "0"),
      "notzankoku"  -> aParams.notzankoku.map(if (_) "1" else "0"),
      "nottensei"   -> aParams.nottensei.map(if (_) "1" else "0"),
      "nottenni"    -> aParams.nottenni.map(if (_) "1" else "0"),
      "minlen"      -> aParams.minlen.map(_.toString),
      "maxlen"      -> aParams.maxlen.map(_.toString),
      "length"      -> aParams.length.map(_.toString),
      "mintime"     -> aParams.mintime.map(_.toString),
      "maxtime"     -> aParams.maxtime.map(_.toString),
      "time"        -> aParams.time.map(_.toString),
      "kaiwaritu"   -> aParams.kaiwaritu.map(_.toString),
      "sasie"       -> aParams.sasie.map(_.toString),
      "type"        -> aParams.`type`.map(_.id.toString),
      "buntai"      -> aParams.buntai.map(_.toString),
      "stop"        -> aParams.stop.map(if (_) "1" else "0"),
      "ncode_out"   -> aParams.ncode_out.map(if (_) "1" else "0"),
      "pickup"      -> aParams.pickup.map(if (_) "1" else "0"),
      "lastup"      -> aParams.lastup,
      "firstup"     -> aParams.firstup,
      "impdate"     -> aParams.impdate,
      "order"       -> aParams.order,
      "lim"         -> aParams.lim.map(_.toString),
      "st"          -> aParams.st.map(_.toString),
      "opt"         -> aParams.opt
    ).collect { case (tKey, Some(tValue)) => s"$tKey=$tValue" }

    // 常にJSON出力を要求
    val tAllParams = tBaseQueryMap ++ Seq(Option("out=json"), mGzipQuery).flatten

    tAllParams.mkString("&")
  }

  override def getNovelTable(ncode: String): Task[List[NovelBody]] = {
    val tUrl     = s"https://ncode.syosetu.com/$ncode/"
    val tRequest = basicRequest.get(uri"$tUrl").readTimeout(30.seconds)

    backend
      .send(tRequest)
      .map { tResponse =>
        tResponse.body match {
          case Right(tHtml) => parseNovelTable(tHtml, ncode)
          case Left(tError) => throw new RuntimeException(s"Failed to fetch novel table: $tError")
        }
      }
  }

  override def getNovelBody(ncode: String, page: Int): Task[NovelBody] = {
    if (page == 0) {
      Task.raiseError(new IllegalArgumentException("Page number must be greater than 0"))
    } else {
      val tUrl     = s"https://ncode.syosetu.com/$ncode/$page/"
      val tRequest = basicRequest.get(uri"$tUrl").readTimeout(30.seconds)

      backend
        .send(tRequest)
        .map { tResponse =>
          tResponse.body match {
            case Right(tHtml) => parseNovelBody(tHtml, ncode, page)
            case Left(tError) => throw new RuntimeException(s"Failed to fetch novel body: $tError")
          }
        }
    }
  }

  private def parseNovelTable(html: String, ncode: String): List[NovelBody] = {
    // HTMLパースは各プラットフォームで実装する（プラットフォーム固有のHTMLパーサーを使用）
    parseNovelTablePlatform(html, ncode)
  }

  private def parseNovelBody(html: String, ncode: String, page: Int): NovelBody = {
    // HTMLパースは各プラットフォームで実装する（プラットフォーム固有のHTMLパーサーを使用）
    parseNovelBodyPlatform(html, ncode, page)
  }

  // プラットフォーム固有のHTMLパース機能（各プラットフォームで実装）
  protected def parseNovelTablePlatform(html: String, ncode: String): List[NovelBody]
  protected def parseNovelBodyPlatform(html: String, ncode: String, page: Int): NovelBody

}
