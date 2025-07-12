package jp.seraphr.narou.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

package object model {

  /**
   * 小説の本文や目次に関する情報
   * @param ncode Nコード
   * @param page ページ番号
   * @param title タイトル（章タイトルまたは話数タイトル）
   * @param body 本文（章の場合は空）
   * @param isChapter 章かどうか
   */
  case class NovelBody(
      ncode: String = "",
      page: Int = 0,
      title: String = "",
      body: String = "",
      isChapter: Boolean = false
  )

  enum ParamRange {

    /** min 以上のものを検索する */
    case MinOnly(min: Int)

    /** max 以下のものを検索する */
    case MaxOnly(max: Int)

    /** min 以上、max 以下のものを検索する */
    case MinMax(min: Int, max: Int)

    /** value の値と完全一致するものを検索する */
    case Exact(value: Int)
  }

  /**
   * 小説APIのレスポンス（配列形式）
   * APIは [{"allcount": ...}, {"title": ..., "ncode": ...}, ...] の形式で返す
   */
  case class NovelApiResponse(
      allcount: Int,
      novels: List[NovelInfo]
  )

  enum BigGenre(val id: Int, val name: String) {
    case Unselected extends BigGenre(0, "未選択")
    case Romance    extends BigGenre(1, "恋愛")
    case Fantasy    extends BigGenre(2, "ファンタジー")
    case Literature extends BigGenre(3, "文芸")
    case SF         extends BigGenre(4, "SF")
    case NonGenre   extends BigGenre(98, "ノンジャンル")
    case Other      extends BigGenre(99, "その他")
  }

  enum Genre(val id: Int, val name: String) {
    case Unselected     extends Genre(0, "未選択")
    // 恋愛
    case RomanceIsekai  extends Genre(101, "異世界〔恋愛〕")
    case RomanceReality extends Genre(102, "現実世界〔恋愛〕")
    // ファンタジー
    case HighFantasy    extends Genre(201, "ハイファンタジー〔ファンタジー〕")
    case LowFantasy     extends Genre(202, "ローファンタジー〔ファンタジー〕")
    // 文芸
    case PureLiterature extends Genre(301, "純文学〔文芸〕")
    case HumanDrama     extends Genre(302, "ヒューマンドラマ〔文芸〕")
    case History        extends Genre(303, "歴史〔文芸〕")
    case Mystery        extends Genre(304, "推理〔文芸〕")
    case Horror         extends Genre(305, "ホラー〔文芸〕")
    case Action         extends Genre(306, "アクション〔文芸〕")
    case Comedy         extends Genre(307, "コメディー〔文芸〕")
    // SF
    case VRGame         extends Genre(401, "VRゲーム〔SF〕")
    case Space          extends Genre(402, "宇宙〔SF〕")
    case Science        extends Genre(403, "空想科学〔SF〕")
    case Panic          extends Genre(404, "パニック〔SF〕")
    // その他
    case Fairy          extends Genre(9901, "童話〔その他〕")
    case Poetry         extends Genre(9902, "詩〔その他〕")
    case Essay          extends Genre(9903, "エッセイ〔その他〕")
    case Replay         extends Genre(9904, "リプレイ〔その他〕")
    case OtherMisc      extends Genre(9999, "その他")
    case NonGenreDetail extends Genre(9801, "ノンジャンル")
  }

  enum NovelType(val id: Int, val name: String) {
    case Short  extends NovelType(1, "短編")
    case Serial extends NovelType(2, "連載")
  }

  /**
   * なろう小説APIから取得される小説情報
   *
   *  @param title 小説名
   *  @param ncode 小説のユニークコード（Nコード）
   *  @param userid 作者のユーザID
   *  @param writer 作者名
   *  @param story 小説のあらすじ
   *  @param biggenre 大ジャンル
   *  @param genre 詳細ジャンル
   *  @param gensaku 原作の有無（なし:空文字、有り:原作名）
   *  @param keyword キーワード（スペース区切り）
   *  @param general_firstup 初回掲載日時（YYYY-MM-DD HH:MM:SS）
   *  @param general_lastup 最終掲載日時（YYYY-MM-DD HH:MM:SS）
   *  @param novel_type 小説の種類
   *  @param end 完結済みかどうか
   *  @param general_all_no 全話数
   *  @param length 総文字数
   *  @param time 読了時間（分）
   *  @param isstop 長期間連載停止中かどうか
   *  @param isr15 R15指定
   *  @param isbl ボーイズラブ指定
   *  @param isgl ガールズラブ指定
   *  @param iszankoku 残酷な描写指定
   *  @param istensei 異世界転生指定
   *  @param istenni 異世界転移指定
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
      biggenre: BigGenre,
      genre: Genre,
      gensaku: String,
      keyword: String,
      general_firstup: String,
      general_lastup: String,
      novel_type: NovelType,
      end: Boolean,
      general_all_no: Int,
      length: Int,
      time: Int,
      isstop: Boolean,
      isr15: Boolean,
      isbl: Boolean,
      isgl: Boolean,
      iszankoku: Boolean,
      istensei: Boolean,
      istenni: Boolean,
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
   *  @param biggenre 大ジャンル
   *  @param notbiggenre 除外大ジャンル
   *  @param genre 詳細ジャンル
   *  @param notgenre 除外詳細ジャンル
   *  @param userid 作者のユーザID
   *  @param ncode 小説のユニークコード（Nコード）
   *  @param title 小説名完全一致
   *  @param ex 除外小説名完全一致
   *  @param keyword キーワード完全一致
   *  @param wname 作者名完全一致
   *  @param isr15 R15指定作品のみ
   *  @param isbl ボーイズラブ作品のみ
   *  @param isgl ガールズラブ作品のみ
   *  @param iszankoku 残酷描写作品のみ
   *  @param istensei 異世界転生作品のみ
   *  @param istenni 異世界転移作品のみ
   *  @param notr15 R15指定作品除外
   *  @param notbl ボーイズラブ作品除外
   *  @param notgl ガールズラブ作品除外
   *  @param notzankoku 残酷描写作品除外
   *  @param nottensei 異世界転生作品除外
   *  @param nottenni 異世界転移作品除外
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
      biggenre: Option[BigGenre] = None,
      notbiggenre: Option[BigGenre] = None,
      genre: Option[Genre] = None,
      notgenre: Option[Genre] = None,
      userid: Option[Int] = None,
      ncode: Option[String] = None,
      title: Option[String] = None,
      ex: Option[String] = None,
      keyword: Option[String] = None,
      wname: Option[String] = None,
      isr15: Option[Boolean] = None,
      isbl: Option[Boolean] = None,
      isgl: Option[Boolean] = None,
      iszankoku: Option[Boolean] = None,
      istensei: Option[Boolean] = None,
      istenni: Option[Boolean] = None,
      notr15: Option[Boolean] = None,
      notbl: Option[Boolean] = None,
      notgl: Option[Boolean] = None,
      notzankoku: Option[Boolean] = None,
      nottensei: Option[Boolean] = None,
      nottenni: Option[Boolean] = None,
      minlen: Option[Int] = None,
      maxlen: Option[Int] = None,
      length: Option[Int] = None,
      mintime: Option[Int] = None,
      maxtime: Option[Int] = None,
      time: Option[Int] = None,
      kaiwaritu: Option[Int] = None,
      sasie: Option[Int] = None,
      `type`: Option[NovelType] = None,
      buntai: Option[Int] = None,
      stop: Option[Boolean] = None,
      ncode_out: Option[Boolean] = None,
      pickup: Option[Boolean] = None,
      lastup: Option[String] = None,
      firstup: Option[String] = None,
      impdate: Option[String] = None,
      order: Option[String] = None,
      lim: Option[Int] = None,
      st: Option[Int] = None,
      opt: Option[String] = None
  )

  // Enum用のコンパニオンオブジェクトを追加
  object BigGenre {

    /** IDからBigGenreを取得します */
    def fromId(aId: Int): Option[BigGenre] = BigGenre.values.find(_.id == aId)
  }

  object Genre {

    /** IDからGenreを取得します */
    def fromId(aId: Int): Option[Genre] = Genre.values.find(_.id == aId)
  }

  object NovelType {

    /** IDからNovelTypeを取得します */
    def fromId(aId: Int): Option[NovelType] = NovelType.values.find(_.id == aId)
  }

  // カスタムデコーダー/エンコーダー
  implicit val bigGenreDecoder: Decoder[BigGenre] = Decoder
    .decodeInt
    .emap { id =>
      BigGenre.fromId(id).toRight(s"Invalid BigGenre id: $id")
    }

  implicit val bigGenreEncoder: Encoder[BigGenre] = Encoder.encodeInt.contramap(_.id)

  implicit val genreDecoder: Decoder[Genre] = Decoder
    .decodeInt
    .emap { id =>
      Genre.fromId(id).toRight(s"Invalid Genre id: $id")
    }

  implicit val genreEncoder: Encoder[Genre] = Encoder.encodeInt.contramap(_.id)

  implicit val novelTypeDecoder: Decoder[NovelType] = Decoder
    .decodeInt
    .emap { id =>
      NovelType.fromId(id).toRight(s"Invalid NovelType id: $id")
    }

  implicit val novelTypeEncoder: Encoder[NovelType] = Encoder.encodeInt.contramap(_.id)

  // Boolean型のAPIフィールド用（0/1 <-> Boolean）
  implicit val booleanFromIntDecoder: Decoder[Boolean] = Decoder.decodeInt.map(_ == 1)
  implicit val booleanToIntEncoder: Encoder[Boolean]   = Encoder.encodeInt.contramap(if (_) 1 else 0)

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

  implicit val novelBodyDecoder: Decoder[NovelBody] = deriveDecoder[NovelBody]
  implicit val novelBodyEncoder: Encoder[NovelBody] = deriveEncoder[NovelBody]

  implicit val searchParamsEncoder: Encoder[SearchParams] = deriveEncoder[SearchParams]
}
