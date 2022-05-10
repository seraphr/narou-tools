package jp.seraphr.narou

import narou4j.enums.NovelGenre
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class NovelCollectorTest extends AnyFreeSpec with Matchers {
  "Filter includeテスト" in {
    val tEmptyKeyword        = SearchFilter(NovelGenre.OTHER, Some(true), Set())
    val tAllNonEmptyKeywords = KeywordFilter
      .all
      .subsets()
      .filter(_.nonEmpty)
      .map {
        SearchFilter(NovelGenre.OTHER, Some(true), _)
      }

    tAllNonEmptyKeywords.foreach { tNotEmptyKeyword =>
      tEmptyKeyword.include(tNotEmptyKeyword) shouldBe true
    }
  }
}
