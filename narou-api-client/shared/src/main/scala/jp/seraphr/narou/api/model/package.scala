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

  /** 検索対象フラグ（1:対象にする、0:対象にしない） */
  enum SearchTargetFlag(val value: Int) {
    case Include extends SearchTargetFlag(1)
    case Exclude extends SearchTargetFlag(0)
  }

  /** 並び順の種類 */
  enum OrderType(val value: String, val name: String) {
    case New            extends OrderType("new", "新着更新順")
    case FavNovelCnt    extends OrderType("favnovelcnt", "ブックマーク数の多い順")
    case ReviewCnt      extends OrderType("reviewcnt", "レビュー数の多い順")
    case Hyoka          extends OrderType("hyoka", "総合ポイントの高い順")
    case HyokaAsc       extends OrderType("hyokaasc", "総合ポイントの低い順")
    case DailyPoint     extends OrderType("dailypoint", "日間ポイントの高い順")
    case WeeklyPoint    extends OrderType("weeklypoint", "週間ポイントの高い順")
    case MonthlyPoint   extends OrderType("monthlypoint", "月間ポイントの高い順")
    case QuarterPoint   extends OrderType("quarterpoint", "四半期ポイントの高い順")
    case YearlyPoint    extends OrderType("yearlypoint", "年間ポイントの高い順")
    case ImpressionCnt  extends OrderType("impressioncnt", "感想の多い順")
    case HyokaCnt       extends OrderType("hyokacnt", "評価者数の多い順")
    case HyokaCntAsc    extends OrderType("hyokacntasc", "評価者数の少ない順")
    case Weekly         extends OrderType("weekly", "週間ユニークユーザの多い順")
    case LengthDesc     extends OrderType("lengthdesc", "作品本文の文字数が多い順")
    case LengthAsc      extends OrderType("lengthasc", "作品本文の文字数が少ない順")
    case GeneralFirstUp extends OrderType("generalfirstup", "初回掲載順")
    case NcodeAsc       extends OrderType("ncodeasc", "Nコード昇順")
    case NcodeDesc      extends OrderType("ncodedesc", "Nコード降順")
    case Old            extends OrderType("old", "更新が古い順")
  }

  /** 文体の種類 */
  enum BuntaiType(val id: Int, val name: String) {
    case NoIndentManyBreaks    extends BuntaiType(1, "字下げされておらず、連続改行が多い作品")
    case NoIndentAverageBreaks extends BuntaiType(2, "字下げされていないが、改行数は平均な作品")
    case IndentManyBreaks      extends BuntaiType(4, "字下げが適切だが、連続改行が多い作品")
    case IndentAverageBreaks   extends BuntaiType(6, "字下げが適切でかつ改行数も平均な作品")
  }

  /** 連載停止状況 */
  enum StopStatus(val id: Int, val name: String) {
    case ExcludeLongStop extends StopStatus(1, "長期連載停止中を除く")
    case OnlyLongStop    extends StopStatus(2, "長期連載停止中のみ取得")
  }

  /** 日付範囲指定 */
  enum DateRange(val value: String, val name: String) {
    case ThisWeek  extends DateRange("thisweek", "今週")
    case LastWeek  extends DateRange("lastweek", "先週")
    case SevenDay  extends DateRange("sevenday", "過去7日間")
    case ThisMonth extends DateRange("thismonth", "今月")
    case LastMonth extends DateRange("lastmonth", "先月")

    /** UNIXタイムスタンプ範囲（例: 1262271600-1264949999） */
    case TimestampRange(start: Long, end: Long) extends DateRange(s"$start-$end", s"$start-$end")
  }

  /** 作品種別 */
  enum NovelTypeFilter(val value: String, val name: String) {
    case Short             extends NovelTypeFilter("t", "短編")
    case Serial            extends NovelTypeFilter("r", "連載中")
    case CompletedSerial   extends NovelTypeFilter("er", "完結済連載作品")
    case AllSerial         extends NovelTypeFilter("re", "すべての連載作品（連載中および完結済）")
    case ShortAndCompleted extends NovelTypeFilter("ter", "短編と完結済連載作品")
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
   *  @param biggenre 大ジャンル（複数指定可能）
   *  @param notbiggenre 除外大ジャンル（複数指定可能）
   *  @param genre 詳細ジャンル（複数指定可能）
   *  @param notgenre 除外詳細ジャンル（複数指定可能）
   *  @param userid 作者のユーザID（複数指定可能）
   *  @param ncode 小説のユニークコード（Nコード、複数指定可能）
   *  @param title タイトルを検索対象にするか（1:対象にする、0:対象にしない）
   *  @param ex あらすじを検索対象にするか（1:対象にする、0:対象にしない）
   *  @param keyword キーワードを検索対象にするか（1:対象にする、0:対象にしない）
   *  @param wname 作者名を検索対象にするか（1:対象にする、0:対象にしない）
   *  @param isr15 R15指定作品のみ
   *  @param isbl ボーイズラブ作品のみ
   *  @param isgl ガールズラブ作品のみ
   *  @param iszankoku 残酷描写作品のみ
   *  @param istensei 異世界転生作品のみ
   *  @param istenni 異世界転移作品のみ
   *  @param istt 異世界転生または異世界転移作品のみ
   *  @param notr15 R15指定作品除外
   *  @param notbl ボーイズラブ作品除外
   *  @param notgl ガールズラブ作品除外
   *  @param notzankoku 残酷描写作品除外
   *  @param nottensei 異世界転生作品除外
   *  @param nottenni 異世界転移作品除外
   *  @param minlen 最小文字数
   *  @param maxlen 最大文字数
   *  @param length 文字数範囲指定
   *  @param mintime 最小読了時間（分）
   *  @param maxtime 最大読了時間（分）
   *  @param time 読了時間範囲指定
   *  @param kaiwaritu 会話率範囲指定
   *  @param sasie 挿絵数範囲指定
   *  @param `type` 作品種別フィルター
   *  @param buntai 文体（複数指定可能）
   *  @param stop 連載停止状況
   *  @param ispickup ピックアップ対象（1:対象のみ）
   *  @param lastup 最終掲載日範囲指定
   *  @param lastupdate 最終更新日範囲指定
   *  @param order 並び順
   *  @param lim 取得件数（1-500、デフォルト20）
   *  @param st 開始位置（1から開始、デフォルト1）
   *  @param opt 追加出力オプション（weekly等）
   */
  case class SearchParams(
      // 検索キーワード系
      word: Option[String] = None,
      notword: Option[String] = None,

      // 検索対象指定系
      title: Option[SearchTargetFlag] = None,
      ex: Option[SearchTargetFlag] = None,
      keyword: Option[SearchTargetFlag] = None,
      wname: Option[SearchTargetFlag] = None,

      // ジャンル系（複数指定可能）
      biggenre: Seq[BigGenre] = Seq.empty,
      notbiggenre: Seq[BigGenre] = Seq.empty,
      genre: Seq[Genre] = Seq.empty,
      notgenre: Seq[Genre] = Seq.empty,

      // 作者・作品特定系（複数指定可能）
      userid: Seq[String] = Seq.empty,
      ncode: Seq[String] = Seq.empty,

      // 内容要素系（含む）
      isr15: Option[Boolean] = None,
      isbl: Option[Boolean] = None,
      isgl: Option[Boolean] = None,
      iszankoku: Option[Boolean] = None,
      istensei: Option[Boolean] = None,
      istenni: Option[Boolean] = None,
      istt: Option[Boolean] = None,

      // 内容要素系（除外）
      notr15: Option[Boolean] = None,
      notbl: Option[Boolean] = None,
      notgl: Option[Boolean] = None,
      notzankoku: Option[Boolean] = None,
      nottensei: Option[Boolean] = None,
      nottenni: Option[Boolean] = None,

      // 文字数・時間系
      minlen: Option[Int] = None,
      maxlen: Option[Int] = None,
      length: Option[ParamRange] = None,
      mintime: Option[Int] = None,
      maxtime: Option[Int] = None,
      time: Option[ParamRange] = None,

      // 作品特徴系
      kaiwaritu: Option[ParamRange] = None,
      sasie: Option[ParamRange] = None,
      `type`: Option[NovelTypeFilter] = None,
      buntai: Seq[BuntaiType] = Seq.empty,
      stop: Option[StopStatus] = None,

      // 特殊系
      ispickup: Option[Boolean] = None,

      // 日付系
      lastup: Option[DateRange] = None,
      lastupdate: Option[DateRange] = None,

      // 出力制御系
      order: Option[OrderType] = None,
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

  object SearchTargetFlag {

    /** 値からSearchTargetFlagを取得します */
    def fromValue(aValue: Int): Option[SearchTargetFlag] = SearchTargetFlag.values.find(_.value == aValue)
  }

  object OrderType {

    /** 値からOrderTypeを取得します */
    def fromValue(aValue: String): Option[OrderType] = OrderType.values.find(_.value == aValue)
  }

  object BuntaiType {

    /** IDからBuntaiTypeを取得します */
    def fromId(aId: Int): Option[BuntaiType] = BuntaiType.values.find(_.id == aId)
  }

  object StopStatus {

    /** IDからStopStatusを取得します */
    def fromId(aId: Int): Option[StopStatus] = StopStatus.values.find(_.id == aId)
  }

  object DateRange {

    /** 定義済みの値のリスト（TimestampRangeを除く） */
    private val predefinedValues = List(
      DateRange.ThisWeek,
      DateRange.LastWeek,
      DateRange.SevenDay,
      DateRange.ThisMonth,
      DateRange.LastMonth
    )

    /** 値からDateRangeを取得します */
    def fromValue(aValue: String): Option[DateRange] = {
      predefinedValues
        .find(_.value == aValue)
        .orElse {
          // UNIXタイムスタンプ範囲の場合
          if (aValue.contains("-") && aValue.forall(c => c.isDigit || c == '-')) {
            aValue.split("-").toList match {
              case start :: end :: Nil if start.nonEmpty && end.nonEmpty =>
                try {
                  Some(DateRange.TimestampRange(start.toLong, end.toLong))
                } catch {
                  case _: NumberFormatException => None
                }
              case _                                                     => None
            }
          } else None
        }
    }

  }

  object NovelTypeFilter {

    /** 値からNovelTypeFilterを取得します */
    def fromValue(aValue: String): Option[NovelTypeFilter] = NovelTypeFilter.values.find(_.value == aValue)
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

  // 新しいenum用のデコーダー/エンコーダー
  implicit val searchTargetFlagDecoder: Decoder[SearchTargetFlag] = Decoder
    .decodeInt
    .emap { value =>
      SearchTargetFlag.fromValue(value).toRight(s"Invalid SearchTargetFlag value: $value")
    }

  implicit val searchTargetFlagEncoder: Encoder[SearchTargetFlag] = Encoder.encodeInt.contramap(_.value)

  implicit val orderTypeDecoder: Decoder[OrderType] = Decoder
    .decodeString
    .emap { value =>
      OrderType.fromValue(value).toRight(s"Invalid OrderType value: $value")
    }

  implicit val orderTypeEncoder: Encoder[OrderType] = Encoder.encodeString.contramap(_.value)

  implicit val buntaiTypeDecoder: Decoder[BuntaiType] = Decoder
    .decodeInt
    .emap { id =>
      BuntaiType.fromId(id).toRight(s"Invalid BuntaiType id: $id")
    }

  implicit val buntaiTypeEncoder: Encoder[BuntaiType] = Encoder.encodeInt.contramap(_.id)

  implicit val stopStatusDecoder: Decoder[StopStatus] = Decoder
    .decodeInt
    .emap { id =>
      StopStatus.fromId(id).toRight(s"Invalid StopStatus id: $id")
    }

  implicit val stopStatusEncoder: Encoder[StopStatus] = Encoder.encodeInt.contramap(_.id)

  implicit val dateRangeDecoder: Decoder[DateRange] = Decoder
    .decodeString
    .emap { value =>
      DateRange.fromValue(value).toRight(s"Invalid DateRange value: $value")
    }

  implicit val dateRangeEncoder: Encoder[DateRange] = Encoder.encodeString.contramap(_.value)

  implicit val novelTypeFilterDecoder: Decoder[NovelTypeFilter] = Decoder
    .decodeString
    .emap { value =>
      NovelTypeFilter.fromValue(value).toRight(s"Invalid NovelTypeFilter value: $value")
    }

  implicit val novelTypeFilterEncoder: Encoder[NovelTypeFilter] = Encoder.encodeString.contramap(_.value)

  // ParamRange用の特殊なエンコーダー（API形式への変換）
  implicit val paramRangeEncoder: Encoder[ParamRange] = Encoder
    .encodeString
    .contramap { range =>
      range match {
        case ParamRange.MinOnly(min)     => s"$min-"
        case ParamRange.MaxOnly(max)     => s"-$max"
        case ParamRange.MinMax(min, max) => s"$min-$max"
        case ParamRange.Exact(value)     => value.toString
      }
    }

  implicit val paramRangeDecoder: Decoder[ParamRange] = Decoder
    .decodeString
    .emap { str =>
      if (str.contains("-")) {
        str.split("-").toList match {
          case "" :: max :: Nil if max.nonEmpty                  =>
            try {
              Right(ParamRange.MaxOnly(max.toInt))
            } catch {
              case _: NumberFormatException => Left(s"Invalid max value in ParamRange: $max")
            }
          case min :: "" :: Nil if min.nonEmpty                  =>
            try {
              Right(ParamRange.MinOnly(min.toInt))
            } catch {
              case _: NumberFormatException => Left(s"Invalid min value in ParamRange: $min")
            }
          case min :: max :: Nil if min.nonEmpty && max.nonEmpty =>
            try {
              Right(ParamRange.MinMax(min.toInt, max.toInt))
            } catch {
              case _: NumberFormatException => Left(s"Invalid min-max values in ParamRange: $min-$max")
            }
          case _                                                 => Left(s"Invalid ParamRange format: $str")
        }
      } else {
        try {
          Right(ParamRange.Exact(str.toInt))
        } catch {
          case _: NumberFormatException => Left(s"Invalid exact value in ParamRange: $str")
        }
      }
    }

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
