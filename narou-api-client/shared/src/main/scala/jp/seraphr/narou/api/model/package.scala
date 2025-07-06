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

  /**
   * なろう小説APIから取得される小説情報
   *
   *  @param title 小説名
   *  @param ncode 小説のユニークコード（Nコード）
   *  @param userid 作者のユーザID
   *  @param writer 作者名
   *  @param story 小説のあらすじ
   *  @param biggenre 大ジャンル（1:恋愛 2:ファンタジー 3:文芸 4:SF 99:その他 98:ノンジャンル）
   *  @param genre 詳細ジャンル（101:異世界〔恋愛〕 102:現実世界〔恋愛〕 201:ハイファンタジー〔ファンタジー〕 202:ローファンタジー〔ファンタジー〕等）
   *  @param gensaku 原作の有無（なし:空文字、有り:原作名）
   *  @param keyword キーワード（スペース区切り）
   *  @param general_firstup 初回掲載日時（YYYY-MM-DD HH:MM:SS）
   *  @param general_lastup 最終掲載日時（YYYY-MM-DD HH:MM:SS）
   *  @param novel_type 小説の種類（1:短編 2:連載中 3:完結済み）
   *  @param end 完結済みかどうか（0:連載中 1:完結済み）
   *  @param general_all_no 全話数
   *  @param length 総文字数
   *  @param time 読了時間（分）
   *  @param isstop 長期間連載停止中かどうか（0:連載中 1:停止中）
   *  @param isr15 R15指定（0:なし 1:あり）
   *  @param isbl ボーイズラブ指定（0:なし 1:あり）
   *  @param isgl ガールズラブ指定（0:なし 1:あり）
   *  @param iszankoku 残酷な描写指定（0:なし 1:あり）
   *  @param istensei 異世界転生指定（0:なし 1:あり）
   *  @param istenni 異世界転移指定（0:なし 1:あり）
   *  @param global_point 総合評価ポイント
   *  @param daily_point 日間ポイント
   *  @param weekly_point 週間ポイント
   *  @param monthly_point 月間ポイント
   *  @param quarter_point 四半期ポイント
   *  @param yearly_point 年間ポイント
   *  @param fav_novel_cnt ブックマーク数
   *  @param impression_cnt 感想数
   *  @param review_cnt レビュー数
   *  @param all_point 評価ポイント
   *  @param all_hyoka_cnt 評価者数
   *  @param sasie_cnt 挿絵数
   *  @param kaiwaritu 会話率（%）
   *  @param novelupdated_at 小説の更新日時（YYYY-MM-DD HH:MM:SS）
   *  @param updated_at データ更新日時（YYYY-MM-DD HH:MM:SS）
   */
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

  /**
   * なろう小説APIの検索パラメータ
   *
   *  @param word 検索キーワード（部分一致、スペース区切りでAND検索）
   *  @param notword 除外キーワード（部分一致、スペース区切りでAND検索）
   *  @param biggenre 大ジャンル（1:恋愛 2:ファンタジー 3:文芸 4:SF 99:その他 98:ノンジャンル）
   *  @param notbiggenre 除外大ジャンル
   *  @param genre 詳細ジャンル（101:異世界〔恋愛〕 102:現実世界〔恋愛〕 201:ハイファンタジー〔ファンタジー〕等）
   *  @param notgenre 除外詳細ジャンル
   *  @param userid 作者のユーザID
   *  @param ncode 小説のユニークコード（Nコード）
   *  @param title 小説名完全一致
   *  @param ex 除外小説名完全一致
   *  @param keyword キーワード完全一致
   *  @param wname 作者名完全一致
   *  @param isr15 R15指定作品のみ（1:含む）
   *  @param isbl ボーイズラブ作品のみ（1:含む）
   *  @param isgl ガールズラブ作品のみ（1:含む）
   *  @param iszankoku 残酷描写作品のみ（1:含む）
   *  @param istensei 異世界転生作品のみ（1:含む）
   *  @param istenni 異世界転移作品のみ（1:含む）
   *  @param notr15 R15指定作品除外（1:除外）
   *  @param notbl ボーイズラブ作品除外（1:除外）
   *  @param notgl ガールズラブ作品除外（1:除外）
   *  @param notzankoku 残酷描写作品除外（1:除外）
   *  @param nottensei 異世界転生作品除外（1:除外）
   *  @param nottenni 異世界転移作品除外（1:除外）
   *  @param minlen 最小文字数
   *  @param maxlen 最大文字数
   *  @param length 文字数範囲（1-4、1:短編 2:中編 3:長編 4:超長編）
   *  @param mintime 最小読了時間（分）
   *  @param maxtime 最大読了時間（分）
   *  @param time 読了時間範囲（1-4、1:短時間 2:中時間 3:長時間 4:超長時間）
   *  @param kaiwaritu 会話率（1-3、1:多め 2:普通 3:少なめ）
   *  @param sasie 挿絵数（1-3、1:多め 2:普通 3:少なめ）
   *  @param type 小説種別（1:短編 2:連載中 3:完結済み）
   *  @param buntai 文体（1:です・ます調 2:だ・である調）
   *  @param stop 連載停止状況（1:長期連載停止中を除く）
   *  @param ncode_out Nコード出力（1:出力）
   *  @param pickup ピックアップ対象（1:対象のみ）
   *  @param lastup 最終掲載日（YYYY-MM-DD）
   *  @param firstup 初回掲載日（YYYY-MM-DD）
   *  @param impdate 最終更新日（YYYY-MM-DD）
   *  @param order 並び順（new:新着 favnovelcnt:ブックマーク数 reviewcnt:レビュー数 hyoka:総合評価 等）
   *  @param lim 取得件数（1-500、デフォルト20）
   *  @param st 開始位置（1から開始、デフォルト1）
   *  @param of 出力フィールド（複数指定時は-で区切り、デフォルト全項目）
   *  @param opt 追加出力オプション（weekly:週間ランキング monthly:月間ランキング 等）
   *  @param out 出力形式（json:JSON形式、デフォルト）
   */
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
      isr15: Option[Int] = None,
      isbl: Option[Int] = None,
      isgl: Option[Int] = None,
      iszankoku: Option[Int] = None,
      istensei: Option[Int] = None,
      istenni: Option[Int] = None,
      notr15: Option[Int] = None,
      notbl: Option[Int] = None,
      notgl: Option[Int] = None,
      notzankoku: Option[Int] = None,
      nottensei: Option[Int] = None,
      nottenni: Option[Int] = None,
      minlen: Option[Int] = None,
      maxlen: Option[Int] = None,
      length: Option[Int] = None,
      mintime: Option[Int] = None,
      maxtime: Option[Int] = None,
      time: Option[Int] = None,
      kaiwaritu: Option[Int] = None,
      sasie: Option[Int] = None,
      `type`: Option[Int] = None,
      buntai: Option[Int] = None,
      stop: Option[Int] = None,
      ncode_out: Option[Int] = None,
      pickup: Option[Int] = None,
      lastup: Option[String] = None,
      firstup: Option[String] = None,
      impdate: Option[String] = None,
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
