package jp.seraphr.narou.model

import scala.scalajs.js.annotation.JSExportAll

case class Genre(id: Int, text: String)

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
