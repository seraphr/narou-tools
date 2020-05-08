package jp.seraphr.narou.model

import java.util.Date

case class NarouNovelsMeta(
  createdAt: Date,
  novelCount: Int,
  novelFiles: Seq[String]
)
