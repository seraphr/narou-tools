package jp.seraphr.narou.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

package object model {

  /**
   * 小説APIのレスポンス（配列形式）
   * APIは [{"allcount": ...}, {"title": ..., "ncode": ...}, ...] の形式で返す
   */
  case class NovelApiResponse(
      allcount: Int,
      novels: List[NovelInfo]
  )

  /** 小説情報 */
  case class NovelInfo(
      title: String,
      ncode: String,
      userid: Int,
      writer: String,
      story: String,
      biggenre: Int,
      genre: Int,
      gensaku: String,
      keyword: String,
      general_firstup: String,
      general_lastup: String,
      novel_type: Int,
      end: Int,
      general_all_no: Int,
      length: Int,
      time: Int,
      isstop: Int,
      isr15: Int,
      isbl: Int,
      isgl: Int,
      iszankoku: Int,
      istensei: Int,
      istenni: Int,
      global_point: Int,
      daily_point: Int,
      weekly_point: Int,
      monthly_point: Int,
      quarter_point: Int,
      yearly_point: Int,
      fav_novel_cnt: Int,
      impression_cnt: Int,
      review_cnt: Int,
      all_point: Int,
      all_hyoka_cnt: Int,
      sasie_cnt: Int,
      kaiwaritu: Int,
      novelupdated_at: String,
      updated_at: String
  )

  /** API検索パラメータ */
  case class SearchParams(
      word: Option[String] = None,
      notword: Option[String] = None,
      biggenre: Option[Int] = None,
      notbiggenre: Option[Int] = None,
      genre: Option[Int] = None,
      notgenre: Option[Int] = None,
      userid: Option[Int] = None,
      ncode: Option[String] = None,
      title: Option[String] = None,
      ex: Option[String] = None,
      keyword: Option[String] = None,
      wname: Option[String] = None,
      order: Option[String] = None,
      lim: Option[Int] = None,
      st: Option[Int] = None,
      of: Option[String] = None,
      opt: Option[String] = None,
      out: Option[String] = None
  )

  implicit val novelInfoDecoder: Decoder[NovelInfo] = deriveDecoder[NovelInfo]
  implicit val novelInfoEncoder: Encoder[NovelInfo] = deriveEncoder[NovelInfo]

  // なろう小説APIは配列形式で返すためカスタムデコーダーが必要
  implicit val novelApiResponseDecoder: Decoder[NovelApiResponse] = new Decoder[NovelApiResponse] {
    import io.circe._

    final def apply(c: HCursor): Decoder.Result[NovelApiResponse] = {
      c.as[List[Json]]
        .flatMap { jsonList =>
          if (jsonList.isEmpty) {
            Right(NovelApiResponse(0, List.empty))
          } else {
            // 最初の要素からallcountを取得
            val allcountResult = jsonList.head.hcursor.get[Int]("allcount")

            // 残りの要素を小説情報として解析（手動でtraverseを実装）
            val novelsResult = jsonList
              .tail
              .foldLeft[Either[DecodingFailure, List[NovelInfo]]](Right(List.empty)) { (acc, json) =>
                acc.flatMap { list =>
                  json.as[NovelInfo].map(novel => list :+ novel)
                }
              }

            for {
              allcount <- allcountResult
              novels   <- novelsResult
            } yield NovelApiResponse(allcount, novels)
          }
        }
    }
  }

  implicit val novelApiResponseEncoder: Encoder[NovelApiResponse] = deriveEncoder[NovelApiResponse]

  implicit val searchParamsEncoder: Encoder[SearchParams] = deriveEncoder[SearchParams]
}
