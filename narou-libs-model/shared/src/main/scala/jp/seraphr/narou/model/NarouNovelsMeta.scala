package jp.seraphr.narou.model

import java.util.Date

case class ExtractNarouNovelsMeta(
  conditionDirs: Seq[String]
)

case class NarouNovelsMeta(
  name: String,
  createdAt: Date,
  novelCount: Int,
  novelFiles: Seq[String]
)
