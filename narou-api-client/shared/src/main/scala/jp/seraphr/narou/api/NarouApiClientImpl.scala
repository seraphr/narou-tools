package jp.seraphr.narou.api

import scala.concurrent.duration._

import jp.seraphr.narou.api.model.{ NovelApiResponse, NovelBody, SearchParams }
import jp.seraphr.narou.api.model.given

import monix.eval.Task
import sttp.client4._
import sttp.client4.circe.{ asJson, deserializeJson }

/**
 * なろう小説APIクライアントの実装
 * @param aBackend HTTPリクエストを処理するバックエンド
 * @param aGzipDecoder GZIP圧縮データを解凍する関数（オプション）
 */
abstract class NarouApiClientImpl(aBackend: Backend[Task], aGzipDecoder: Option[Array[Byte] => Array[Byte]])
    extends NarouApiClient {

  private val baseUrl    = "https://api.syosetu.com/novelapi/api/"
  private val mGzipQuery = Option.when(aGzipDecoder.isDefined)("gzip=5")

  override def search(aParams: SearchParams): Task[NovelApiResponse] = {
    val tQueryParams = buildQueryParams(aParams)
    val tFullUrl     = s"$baseUrl?$tQueryParams"

    val tAsJson  = aGzipDecoder match {
      case Some(d) =>
        asByteArray.mapRight((ba: Array[Byte]) => new String(d(ba))).map(_.flatMap(deserializeJson[NovelApiResponse]))
      case None    => asJson[NovelApiResponse]
    }
    val tRequest = basicRequest.get(uri"$tFullUrl").response(tAsJson).readTimeout(30.seconds)

    aBackend
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
    import jp.seraphr.narou.api.model._

    // 複数値を「-」区切りの文字列に変換するヘルパー関数
    def seqToHyphenString[T](seq: Seq[T], valueExtractor: T => String): Option[String] = {
      if (seq.nonEmpty) Some(seq.map(valueExtractor).mkString("-")) else None
    }

    // ParamRangeをAPI形式の文字列に変換するヘルパー関数
    def paramRangeToString(range: ParamRange): String = range match {
      case ParamRange.MinOnly(min)     => s"$min-"
      case ParamRange.MaxOnly(max)     => s"-$max"
      case ParamRange.MinMax(min, max) => s"$min-$max"
      case ParamRange.Exact(value)     => value.toString
    }

    val tBaseQueryMap = Map(
      // 検索キーワード系
      "word"    -> aParams.word,
      "notword" -> aParams.notword,

      // 検索対象指定系
      "title"   -> aParams.title.map(_.value.toString),
      "ex"      -> aParams.ex.map(_.value.toString),
      "keyword" -> aParams.keyword.map(_.value.toString),
      "wname"   -> aParams.wname.map(_.value.toString),

      // ジャンル系（複数指定可能）
      "biggenre"    -> seqToHyphenString(aParams.biggenre, (bg: BigGenre) => bg.id.toString),
      "notbiggenre" -> seqToHyphenString(aParams.notbiggenre, (bg: BigGenre) => bg.id.toString),
      "genre"       -> seqToHyphenString(aParams.genre, (g: Genre) => g.id.toString),
      "notgenre"    -> seqToHyphenString(aParams.notgenre, (g: Genre) => g.id.toString),

      // 作者・作品特定系（複数指定可能）
      "userid" -> seqToHyphenString(aParams.userid, identity),
      "ncode"  -> seqToHyphenString(aParams.ncode, identity),

      // 内容要素系（含む）
      "isr15"     -> aParams.isr15.map(if (_) "1" else "0"),
      "isbl"      -> aParams.isbl.map(if (_) "1" else "0"),
      "isgl"      -> aParams.isgl.map(if (_) "1" else "0"),
      "iszankoku" -> aParams.iszankoku.map(if (_) "1" else "0"),
      "istensei"  -> aParams.istensei.map(if (_) "1" else "0"),
      "istenni"   -> aParams.istenni.map(if (_) "1" else "0"),
      "istt"      -> aParams.istt.map(if (_) "1" else "0"),

      // 内容要素系（除外）
      "notr15"     -> aParams.notr15.map(if (_) "1" else "0"),
      "notbl"      -> aParams.notbl.map(if (_) "1" else "0"),
      "notgl"      -> aParams.notgl.map(if (_) "1" else "0"),
      "notzankoku" -> aParams.notzankoku.map(if (_) "1" else "0"),
      "nottensei"  -> aParams.nottensei.map(if (_) "1" else "0"),
      "nottenni"   -> aParams.nottenni.map(if (_) "1" else "0"),

      // 文字数・時間系
      "minlen"  -> aParams.minlen.map(_.toString),
      "maxlen"  -> aParams.maxlen.map(_.toString),
      "length"  -> aParams.length.map(paramRangeToString),
      "mintime" -> aParams.mintime.map(_.toString),
      "maxtime" -> aParams.maxtime.map(_.toString),
      "time"    -> aParams.time.map(paramRangeToString),

      // 作品特徴系
      "kaiwaritu" -> aParams.kaiwaritu.map(paramRangeToString),
      "sasie"     -> aParams.sasie.map(paramRangeToString),
      "type"      -> aParams.`type`.map(_.value),
      "buntai"    -> seqToHyphenString(aParams.buntai, (bt: BuntaiType) => bt.id.toString),
      "stop"      -> aParams.stop.map(_.id.toString),

      // 特殊系
      "ispickup" -> aParams.ispickup.map(if (_) "1" else "0"),

      // 日付系
      "lastup"     -> aParams.lastup.map(_.value),
      "lastupdate" -> aParams.lastupdate.map(_.value),

      // 出力制御系
      "order" -> aParams.order.map(_.value),
      "lim"   -> aParams.lim.map(_.toString),
      "st"    -> aParams.st.map(_.toString),
      "opt"   -> aParams.opt
    ).collect { case (tKey, Some(tValue)) => s"$tKey=$tValue" }

    // 常にJSON出力を要求
    val tAllParams = tBaseQueryMap ++ Seq(Option("out=json"), mGzipQuery).flatten

    tAllParams.mkString("&")
  }

  override def getNovelTable(aNcode: String): Task[List[NovelBody]] = {
    val tUrl     = s"https://ncode.syosetu.com/$aNcode/"
    val tRequest = basicRequest.get(uri"$tUrl").readTimeout(30.seconds)

    aBackend
      .send(tRequest)
      .map { tResponse =>
        tResponse.body match {
          case Right(tHtml) => parseNovelTable(tHtml, aNcode)
          case Left(tError) => throw new RuntimeException(s"Failed to fetch novel table: $tError")
        }
      }
  }

  override def getNovelBody(aNcode: String, aPage: Int): Task[NovelBody] = {
    if (aPage == 0) {
      Task.raiseError(new IllegalArgumentException("Page number must be greater than 0"))
    } else {
      val tUrl     = s"https://ncode.syosetu.com/$aNcode/$aPage/"
      val tRequest = basicRequest.get(uri"$tUrl").readTimeout(30.seconds)

      aBackend
        .send(tRequest)
        .map { tResponse =>
          tResponse.body match {
            case Right(tHtml) => parseNovelBody(tHtml, aNcode, aPage)
            case Left(tError) => throw new RuntimeException(s"Failed to fetch novel body: $tError")
          }
        }
    }
  }

  private def parseNovelTable(aHtml: String, aNcode: String): List[NovelBody] = {
    // HTMLパースは各プラットフォームで実装する（プラットフォーム固有のHTMLパーサーを使用）
    parseNovelTablePlatform(aHtml, aNcode)
  }

  private def parseNovelBody(aHtml: String, aNcode: String, aPage: Int): NovelBody = {
    // HTMLパースは各プラットフォームで実装する（プラットフォーム固有のHTMLパーサーを使用）
    parseNovelBodyPlatform(aHtml, aNcode, aPage)
  }

  // プラットフォーム固有のHTMLパース機能（各プラットフォームで実装）
  protected def parseNovelTablePlatform(aHtml: String, aNcode: String): List[NovelBody]
  protected def parseNovelBodyPlatform(aHtml: String, aNcode: String, aPage: Int): NovelBody

}
