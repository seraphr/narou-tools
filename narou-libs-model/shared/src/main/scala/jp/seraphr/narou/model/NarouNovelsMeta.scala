package jp.seraphr.narou.model

import java.util.Date

/** @param conditionDirs データが格納されているディレクトリ列 */
case class ExtractedNarouNovelsMeta(
    conditionDirs: Seq[String]
)

/**
 * [[ExtractedNarouNovelsMeta]]が保持する各ディレクトリに存在している小説情報のメタデータ
 *
 * @param name 人間可読な名前
 * @param createdAt
 * @param novelCount 何個の小説データが保持されているか
 * @param novelFiles 小説情報を格納しているファイル名の列
 */
case class NarouNovelsMeta(
    name: String,
    createdAt: Date,
    novelCount: Int,
    novelFiles: Seq[String]
)
