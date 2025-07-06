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
