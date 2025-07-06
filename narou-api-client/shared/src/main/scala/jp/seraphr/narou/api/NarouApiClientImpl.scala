package jp.seraphr.narou.api

import scala.concurrent.duration._

import jp.seraphr.narou.api.model.{ NovelApiResponse, SearchParams }
import jp.seraphr.narou.api.model.given

import monix.eval.Task
import sttp.client4._
import sttp.client4.circe.asJson

/** なろう小説APIクライアントの実装 */
class NarouApiClientImpl(backend: Backend[Task]) extends NarouApiClient {

  private val baseUrl = "https://api.syosetu.com/novelapi/api/"

  override def search(aParams: SearchParams): Task[NovelApiResponse] = {
    val tQueryParams = buildQueryParams(aParams)
    val tFullUrl     = s"$baseUrl?$tQueryParams"

    val tRequest = basicRequest.get(uri"$tFullUrl").response(asJson[NovelApiResponse]).readTimeout(30.seconds)

    backend
      .send(tRequest)
      .map { tResponse =>
        tResponse.body match {
          case Right(tApiResponse) => tApiResponse
          case Left(tError)        => throw new RuntimeException(s"API request failed: $tError")
        }
      }
  }

  private def buildQueryParams(aParams: SearchParams): String = {
    val tBaseQueryMap = Map(
      "word"        -> aParams.word,
      "notword"     -> aParams.notword,
      "biggenre"    -> aParams.biggenre.map(_.toString),
      "notbiggenre" -> aParams.notbiggenre.map(_.toString),
      "genre"       -> aParams.genre.map(_.toString),
      "notgenre"    -> aParams.notgenre.map(_.toString),
      "userid"      -> aParams.userid.map(_.toString),
      "ncode"       -> aParams.ncode,
      "title"       -> aParams.title,
      "ex"          -> aParams.ex,
      "keyword"     -> aParams.keyword,
      "wname"       -> aParams.wname,
      "isr15"       -> aParams.isr15.map(_.toString),
      "isbl"        -> aParams.isbl.map(_.toString),
      "isgl"        -> aParams.isgl.map(_.toString),
      "iszankoku"   -> aParams.iszankoku.map(_.toString),
      "istensei"    -> aParams.istensei.map(_.toString),
      "istenni"     -> aParams.istenni.map(_.toString),
      "notr15"      -> aParams.notr15.map(_.toString),
      "notbl"       -> aParams.notbl.map(_.toString),
      "notgl"       -> aParams.notgl.map(_.toString),
      "notzankoku"  -> aParams.notzankoku.map(_.toString),
      "nottensei"   -> aParams.nottensei.map(_.toString),
      "nottenni"    -> aParams.nottenni.map(_.toString),
      "minlen"      -> aParams.minlen.map(_.toString),
      "maxlen"      -> aParams.maxlen.map(_.toString),
      "length"      -> aParams.length.map(_.toString),
      "mintime"     -> aParams.mintime.map(_.toString),
      "maxtime"     -> aParams.maxtime.map(_.toString),
      "time"        -> aParams.time.map(_.toString),
      "kaiwaritu"   -> aParams.kaiwaritu.map(_.toString),
      "sasie"       -> aParams.sasie.map(_.toString),
      "type"        -> aParams.`type`.map(_.toString),
      "buntai"      -> aParams.buntai.map(_.toString),
      "stop"        -> aParams.stop.map(_.toString),
      "ncode_out"   -> aParams.ncode_out.map(_.toString),
      "pickup"      -> aParams.pickup.map(_.toString),
      "lastup"      -> aParams.lastup,
      "firstup"     -> aParams.firstup,
      "impdate"     -> aParams.impdate,
      "order"       -> aParams.order,
      "lim"         -> aParams.lim.map(_.toString),
      "st"          -> aParams.st.map(_.toString),
      "of"          -> aParams.of,
      "opt"         -> aParams.opt
    ).collect { case (tKey, Some(tValue)) => s"$tKey=$tValue" }

    // 常にJSON出力を要求
    val tOutParam  = aParams.out.getOrElse("json")
    val tAllParams = tBaseQueryMap ++ Seq(s"out=$tOutParam")

    tAllParams.mkString("&")
  }

}
