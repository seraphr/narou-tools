package jp.seraphr.narou.model

import scala.scalajs.js.annotation.JSExportAll

enum Genre(val id: Int, val text: String, val bigGenre: BigGenre) {
  case AnotherWorldRomance extends Genre(101, "異世界〔恋愛〕", BigGenre.Romance)
  case Romance             extends Genre(102, "現実世界〔恋愛〕", BigGenre.Romance)
  case HighFantasy         extends Genre(201, "ハイファンタジー〔ファンタジー〕", BigGenre.Fantasy)
  case LowFantasy          extends Genre(202, "ローファンタジー〔ファンタジー〕", BigGenre.Fantasy)
  case PureLiterature      extends Genre(301, "純文学〔文芸〕", BigGenre.Literature)
  case HumanDrama          extends Genre(302, "ヒューマンドラマ〔文芸〕", BigGenre.Literature)
  case History             extends Genre(303, "歴史〔文芸〕", BigGenre.Literature)
  case Detective           extends Genre(304, "推理〔文芸〕", BigGenre.Literature)
  case Horror              extends Genre(305, "ホラー〔文芸〕", BigGenre.Literature)
  case Action              extends Genre(306, "アクション〔文芸〕", BigGenre.Literature)
  case Comedy              extends Genre(307, "コメディー〔文芸〕", BigGenre.Literature)
  case VRGame              extends Genre(401, "VRゲーム〔SF〕", BigGenre.SF)
  case Space               extends Genre(402, "宇宙〔SF〕", BigGenre.SF)
  case SF                  extends Genre(403, "空想科学〔SF〕", BigGenre.SF)
  case Panic               extends Genre(404, "パニック〔SF〕", BigGenre.SF)
  case FairyTale           extends Genre(9901, "童話〔その他〕", BigGenre.Other)
  case Poem                extends Genre(9902, "詩〔その他〕", BigGenre.Other)
  case Essay               extends Genre(9903, "エッセイ〔その他〕", BigGenre.Other)
  case Replay              extends Genre(9904, "リプレイ〔その他〕", BigGenre.Other)
  case Other               extends Genre(9999, "その他〔その他〕", BigGenre.Other)
  case NonGenre            extends Genre(9801, "ノンジャンル〔ノンジャンル〕", BigGenre.NonGenre)
}

object Genre {
  def fromId(id: Int): Either[String, Genre] = {
    Genre.values.find(_.id == id).toRight(s"invalid genre id: $id")
  }

  def fromApiGenre(apiGenre: jp.seraphr.narou.api.model.Genre): Genre = {
    apiGenre match {
      case jp.seraphr.narou.api.model.Genre.RomanceIsekai  => Genre.AnotherWorldRomance
      case jp.seraphr.narou.api.model.Genre.RomanceReality => Genre.Romance
      case jp.seraphr.narou.api.model.Genre.HighFantasy    => Genre.HighFantasy
      case jp.seraphr.narou.api.model.Genre.LowFantasy     => Genre.LowFantasy
      case jp.seraphr.narou.api.model.Genre.PureLiterature => Genre.PureLiterature
      case jp.seraphr.narou.api.model.Genre.HumanDrama     => Genre.HumanDrama
      case jp.seraphr.narou.api.model.Genre.History        => Genre.History
      case jp.seraphr.narou.api.model.Genre.Mystery        => Genre.Detective
      case jp.seraphr.narou.api.model.Genre.Horror         => Genre.Horror
      case jp.seraphr.narou.api.model.Genre.Action         => Genre.Action
      case jp.seraphr.narou.api.model.Genre.Comedy         => Genre.Comedy
      case jp.seraphr.narou.api.model.Genre.VRGame         => Genre.VRGame
      case jp.seraphr.narou.api.model.Genre.Space          => Genre.Space
      case jp.seraphr.narou.api.model.Genre.Science        => Genre.SF
      case jp.seraphr.narou.api.model.Genre.Panic          => Genre.Panic
      case jp.seraphr.narou.api.model.Genre.Fairy          => Genre.FairyTale
      case jp.seraphr.narou.api.model.Genre.Poetry         => Genre.Poem
      case jp.seraphr.narou.api.model.Genre.Essay          => Genre.Essay
      case jp.seraphr.narou.api.model.Genre.Replay         => Genre.Replay
      case jp.seraphr.narou.api.model.Genre.OtherMisc      => Genre.Other
      case jp.seraphr.narou.api.model.Genre.NonGenreDetail => Genre.NonGenre
      case jp.seraphr.narou.api.model.Genre.Unselected     => Genre.Other
    }
  }
}

enum BigGenre(val id: Int) {
  case Romance    extends BigGenre(1)
  case Fantasy    extends BigGenre(2)
  case Literature extends BigGenre(3)
  case SF         extends BigGenre(4)
  case Other      extends BigGenre(99)
  case NonGenre   extends BigGenre(98)
}

enum NovelType(val id: Int) {
  case Serially    extends NovelType(1)
  case ShortStory  extends NovelType(2)
  case Etc(i: Int) extends NovelType(i)
}

enum UploadType {
  case CellularPhone extends UploadType
  case PC            extends UploadType
  case Both          extends UploadType

  /** 更新されてない、古い小説で0になっているものがあるっぽい */
  case Etc(i: Int) extends UploadType
}

/**
 * @param title
 * @param ncode
 * @param userId ユーザId
 * @param writer ユーザ名
 * @param story あらすじ
 * @param genre ジャンル
 * @param gensaku 未使用。 常に空文字列
 * @param keywords
 * @param firstUpload 初アップロード時刻
 * @param lastUpload 最終アップロード時刻
 * @param novelType
 * @param isFinished trueであれば完結済み。　短編はfalse
 * @param chapterCount 全部分数
 * @param length 文字数
 * @param readTimeMinutes 読了時間(分単位)　小説文字数÷500を切り上げした数値
 * @param isR15 必須キーワード R15
 * @param isBL 必須キーワード BL
 * @param isGL 必須キーワード GL
 * @param isZankoku 必須キーワード 残酷
 * @param isTensei 必須キーワード 転生
 * @param isTenni 必須キーワード 転移
 * @param uploadType
 * @param globalPoint 総合評価ポイント(=(ブックマーク数×2)+評価ポイント)
 * @param bookmarkCount ブックマーク数
 * @param reviewCount レビュー数
 * @param evaluationPoint 評価ポイント合計
 * @param evaluationCount 評価数
 * @param illustrationCount 挿絵数
 * @param novelUpdatedAt 最終小説更新時刻。 あらすじ・本文追記などのアップデートでも更新される
 * @param updatedAt 更新時刻。 システム用
 */
@JSExportAll
case class NarouNovel(
    title: String,
    ncode: String,
    userId: String,
    writer: String,
    story: String,
    genre: Genre,
    gensaku: String,
    keywords: Seq[String],
    firstUpload: String,
    lastUpload: String,
    novelType: NovelType,
    isFinished: Boolean,
    chapterCount: Int,
    length: Int,
    readTimeMinutes: Int,
    isR15: Boolean,
    isBL: Boolean,
    isGL: Boolean,
    isZankoku: Boolean,
    isTensei: Boolean,
    isTenni: Boolean,
    uploadType: UploadType,
    globalPoint: Int,
    bookmarkCount: Int,
    reviewCount: Int,
    evaluationPoint: Int,
    evaluationCount: Int,
    illustrationCount: Int,
    novelUpdatedAt: String,
    updatedAt: String
)

object NarouNovel {
  object lens {

    import monocle.macros.GenLens

    val title             = GenLens[NarouNovel](_.title)
    val ncode             = GenLens[NarouNovel](_.ncode)
    val userId            = GenLens[NarouNovel](_.userId)
    val writer            = GenLens[NarouNovel](_.writer)
    val story             = GenLens[NarouNovel](_.story)
    val genre             = GenLens[NarouNovel](_.genre)
    val gensaku           = GenLens[NarouNovel](_.gensaku)
    val keywords          = GenLens[NarouNovel](_.keywords)
    val firstUpload       = GenLens[NarouNovel](_.firstUpload)
    val lastUpload        = GenLens[NarouNovel](_.lastUpload)
    val novelType         = GenLens[NarouNovel](_.novelType)
    val isFinished        = GenLens[NarouNovel](_.isFinished)
    val chapterCount      = GenLens[NarouNovel](_.chapterCount)
    val length            = GenLens[NarouNovel](_.length)
    val readTimeMinutes   = GenLens[NarouNovel](_.readTimeMinutes)
    val isR15             = GenLens[NarouNovel](_.isR15)
    val isBL              = GenLens[NarouNovel](_.isBL)
    val isGL              = GenLens[NarouNovel](_.isGL)
    val isZankoku         = GenLens[NarouNovel](_.isZankoku)
    val isTensei          = GenLens[NarouNovel](_.isTensei)
    val isTenni           = GenLens[NarouNovel](_.isTenni)
    val uploadType        = GenLens[NarouNovel](_.uploadType)
    val globalPoint       = GenLens[NarouNovel](_.globalPoint)
    val bookmarkCount     = GenLens[NarouNovel](_.bookmarkCount)
    val reviewCount       = GenLens[NarouNovel](_.reviewCount)
    val evaluationPoint   = GenLens[NarouNovel](_.evaluationPoint)
    val evaluationCount   = GenLens[NarouNovel](_.evaluationCount)
    val illustrationCount = GenLens[NarouNovel](_.illustrationCount)
    val novelUpdatedAt    = GenLens[NarouNovel](_.novelUpdatedAt)
    val updatedAt         = GenLens[NarouNovel](_.updatedAt)
  }
}
