package jp.seraphr.narou

import scala.collection.mutable.ArrayBuffer

import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.{ BigGenre, Genre, NovelApiResponse, NovelBody, NovelInfo, NovelType, SearchParams }

import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class AllNovelCollectorTest extends AnyFreeSpec with Matchers {
  implicit private val mScheduler: Scheduler = Scheduler.global

  "開始文字数を指定して収集する時、最初の検索条件の文字数下限に指定値が設定されること" in {
    Seq(0, 12345).foreach { tMinLength =>
      val tClient    = new StubNarouApiClient(Seq(NovelApiResponse(0, Nil)))
      val tCollector = new AllNovelCollector(0)

      tCollector
        .collect(NarouClientBuilder.init, tClient, MinLength.from(tMinLength).toOption.get)
        .toVector shouldBe empty
      tClient.searchParams.head.minlen shouldBe Some(tMinLength)
    }
  }

  "開始文字数に負数を指定した時、API呼び出し前に拒否すること" in {
    MinLength.from(-1).isLeft shouldBe true
  }

  "初回検索が0件である時、例外を発生させずに探索を終了すること" in {
    val tClient    = new StubNarouApiClient(Seq(NovelApiResponse(0, Nil)))
    val tCollector = new AllNovelCollector(0)

    noException should be thrownBy tCollector
      .collect(NarouClientBuilder.init, tClient, MinLength.from(5000).toOption.get)
      .toVector
    tClient.searchParams should have size 1
  }

  "ページング途中の検索が0件である時、それまでの結果を返して探索を終了すること" in {
    val tNovels    = Vector.tabulate(500)(tIndex => createNovelInfo(tIndex, 5000)).toList
    val tClient    = new StubNarouApiClient(Seq(NovelApiResponse(500, tNovels), NovelApiResponse(0, Nil)))
    val tCollector = new AllNovelCollector(0)

    tCollector
      .collect(NarouClientBuilder.init, tClient, MinLength.from(5000).toOption.get)
      .toVector should have size 500
    tClient.searchParams should have size 2
    tClient.searchParams(1).minlen shouldBe Some(5000)
    tClient.searchParams(1).st shouldBe Some(500)
  }

  private def createNovelInfo(aIndex: Int, aLength: Int): NovelInfo = {
    NovelInfo(
      title = s"title-${aIndex}",
      ncode = s"N${aIndex}",
      userid = aIndex,
      writer = "writer",
      story = "story",
      biggenre = BigGenre.Fantasy,
      genre = Genre.HighFantasy,
      gensaku = "",
      keyword = "",
      general_firstup = "2020-01-01 00:00:00",
      general_lastup = "2020-01-01 00:00:00",
      novel_type = NovelType.Short,
      end = true,
      general_all_no = 1,
      length = aLength,
      time = 1,
      isstop = false,
      isr15 = false,
      isbl = false,
      isgl = false,
      iszankoku = false,
      istensei = false,
      istenni = false,
      global_point = 0,
      daily_point = 0,
      weekly_point = 0,
      monthly_point = 0,
      quarter_point = 0,
      yearly_point = 0,
      fav_novel_cnt = 0,
      impression_cnt = 0,
      review_cnt = 0,
      all_point = 0,
      all_hyoka_cnt = 0,
      sasie_cnt = 0,
      kaiwaritu = 0,
      novelupdated_at = "2020-01-01 00:00:00",
      updated_at = "2020-01-01 00:00:00"
    )
  }

  private class StubNarouApiClient(aResponses: Seq[NovelApiResponse]) extends NarouApiClient {
    private val mResponses                                             = aResponses.iterator
    val searchParams: ArrayBuffer[SearchParams]                        = ArrayBuffer.empty
    override def search(aParams: SearchParams): Task[NovelApiResponse] = Task.eval {
      searchParams += aParams
      mResponses.next()
    }

    override def getNovelTable(aNcode: String): Task[List[NovelBody]] =
      Task.raiseError(new UnsupportedOperationException)

    override def getNovelBody(aNcode: String, aPage: Int): Task[NovelBody] =
      Task.raiseError(new UnsupportedOperationException)

  }
}
