package jp.seraphr.narou.commands.collect

import java.io.File
import java.nio.file.Files

import scala.collection.mutable.ArrayBuffer

import jp.seraphr.narou.{ DefaultExtractedNovelLoader, ExtractedNarouNovelsWriter, FileNovelDataAccessor }
import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.{ NovelApiResponse, NovelBody, SearchParams }
import jp.seraphr.narou.commands.collect.CollectNovelCommand.{ CollectNovelCommandArg, Local, Recreate, Update }
import jp.seraphr.narou.model.{ Genre, NarouNovel, NovelCondition, NovelType, UploadType }

import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable
import org.apache.commons.io.FileUtils
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class CollectNovelCommandTest extends AnyFreeSpec with Matchers {
  implicit private val mScheduler: Scheduler = Scheduler.global

  "minLengthを省略した時、既定値の5000が使用されること" in {
    CollectNovelCommand.defaultMinLength.value shouldBe 5000
    parse(Seq.empty).map(_.minLength.value) shouldBe Some(5000)
  }

  "minLengthに0または正の整数を指定した時、指定値として解析されること" in {
    Seq(0, 12345).foreach { tMinLength =>
      parse(Seq("--minLength", tMinLength.toString)).map(_.minLength.value) shouldBe Some(tMinLength)
    }
  }

  "minLengthに負数を指定した時、引数解析に失敗すること" in {
    parse(Seq("--minLength", "-1")) shouldBe None
  }

  "minLengthに負数を指定した時、API呼び出しを開始しないこと" in {
    val tClient  = new StubNarouApiClient
    val tCommand = new TestCollectNovelCommand(createDefaultArg(new File("output")), tClient)

    tCommand.run(Seq("--minLength", "-1")).isFailure shouldBe true
    tClient.searchParams shouldBe empty
  }

  "minLengthに0または正の整数を指定して実行した時、最初のAPI検索条件に指定値が渡されること" in {
    val tTempRoot = Files.createTempDirectory("collect-novel-command-test").toFile
    try {
      Seq(0, 12345).foreach { tMinLength =>
        val tClient  = new StubNarouApiClient
        val tCommand = new TestCollectNovelCommand(
          createDefaultArg(new File(tTempRoot, s"output-${tMinLength}")).copy(overwrite = Recreate),
          tClient
        )

        tCommand.run(Seq("--minLength", tMinLength.toString)).isFailure shouldBe true
        tClient.searchParams.head.minlen shouldBe Some(tMinLength)
      }
    } finally {
      FileUtils.deleteDirectory(tTempRoot)
    }
  }

  "APIの初回検索と最終データが0件である時、出力を開始せずに失敗すること" in {
    val tTempRoot = Files.createTempDirectory("collect-novel-command-test").toFile
    try {
      val tOutput  = new File(tTempRoot, "output")
      val tClient  = new StubNarouApiClient
      val tCommand = new TestCollectNovelCommand(createDefaultArg(tOutput).copy(overwrite = Recreate), tClient)

      tCommand.run(Seq.empty).isFailure shouldBe true
      tClient.searchParams.head.minlen shouldBe Some(5000)
      tOutput.exists() shouldBe false
      new File(tTempRoot, "output.bak").exists() shouldBe false
    } finally {
      FileUtils.deleteDirectory(tTempRoot)
    }
  }

  "既存データに開始文字数未満の小説がありAPI検索が0件である時、既存データを保持して正常終了すること" in {
    val tTempRoot = Files.createTempDirectory("collect-novel-command-test").toFile
    try {
      val tOutput   = new File(tTempRoot, "output")
      val tAccessor = new FileNovelDataAccessor(tOutput)
      val tNovel    = createNovel(100)
      new ExtractedNarouNovelsWriter(tAccessor, Seq(NovelCondition.all), 100)
        .write(Observable.now(tNovel))
        .runSyncUnsafe()

      val tClient  = new StubNarouApiClient
      val tCommand = new TestCollectNovelCommand(createDefaultArg(tOutput).copy(withAll = true), tClient)

      tCommand.run(Seq.empty).isSuccess shouldBe true
      tClient.searchParams.head.minlen shouldBe Some(5000)
      new DefaultExtractedNovelLoader(new FileNovelDataAccessor(tOutput)).loadAll.toListL.runSyncUnsafe() should contain(
        tNovel
      )
    } finally {
      FileUtils.deleteDirectory(tTempRoot)
    }
  }

  "既存出力を再生成する時、最終データが0件なら既存出力を変更せずに失敗すること" in {
    val tTempRoot = Files.createTempDirectory("collect-novel-command-test").toFile
    try {
      val tOutput   = new File(tTempRoot, "output")
      val tAccessor = new FileNovelDataAccessor(tOutput)
      val tNovel    = createNovel(100)
      new ExtractedNarouNovelsWriter(tAccessor, Seq(NovelCondition.all), 100)
        .write(Observable.now(tNovel))
        .runSyncUnsafe()

      val tClient  = new StubNarouApiClient
      val tCommand = new TestCollectNovelCommand(createDefaultArg(tOutput).copy(overwrite = Recreate), tClient)

      tCommand.run(Seq.empty).isFailure shouldBe true
      new DefaultExtractedNovelLoader(new FileNovelDataAccessor(tOutput)).loadAll.toListL.runSyncUnsafe() should contain(
        tNovel
      )
      new File(tTempRoot, "output.bak").exists() shouldBe false
    } finally {
      FileUtils.deleteDirectory(tTempRoot)
    }
  }

  private def parse(aArgs: Seq[String]): Option[CollectNovelCommandArg] = {
    val tDefaultArg = createDefaultArg(new File("output"))
    val tCommand    = new CollectNovelCommand(tDefaultArg)
    new tCommand.OptionParser(tDefaultArg).parse(aArgs)
  }

  private def createDefaultArg(aOutput: File): CollectNovelCommandArg = {
    CollectNovelCommandArg(
      output = Local(aOutput),
      overwrite = Update,
      intervalMillis = 0,
      limit = Int.MaxValue,
      minLength = CollectNovelCommand.defaultMinLength,
      novelsPerFile = 10000,
      withAll = false
    )
  }

  private def createNovel(aLength: Int): NarouNovel = {
    NarouNovel(
      title = "title",
      ncode = "N0001",
      userId = "1",
      writer = "writer",
      story = "story",
      genre = Genre.HighFantasy,
      gensaku = "",
      keywords = Seq.empty,
      firstUpload = "2020-01-01 00:00:00",
      lastUpload = "2020-01-01 00:00:00",
      novelType = NovelType.ShortStory,
      isFinished = true,
      chapterCount = 1,
      length = aLength,
      readTimeMinutes = 1,
      isR15 = false,
      isBL = false,
      isGL = false,
      isZankoku = false,
      isTensei = false,
      isTenni = false,
      uploadType = UploadType.PC,
      globalPoint = 0,
      bookmarkCount = 0,
      reviewCount = 0,
      evaluationPoint = 0,
      evaluationCount = 0,
      illustrationCount = 0,
      novelUpdatedAt = "2020-01-01 00:00:00",
      updatedAt = "2020-01-01 00:00:00"
    )
  }

  private class TestCollectNovelCommand(aDefaultArg: CollectNovelCommandArg, aClient: NarouApiClient)
      extends CollectNovelCommand(aDefaultArg) {
    override protected def createNarouApiClient(): NarouApiClient = aClient
  }

  private class StubNarouApiClient extends NarouApiClient {
    val searchParams: ArrayBuffer[SearchParams] = ArrayBuffer.empty

    override def search(aParams: SearchParams): Task[NovelApiResponse] = Task.eval {
      searchParams += aParams
      NovelApiResponse(0, Nil)
    }

    override def getNovelTable(aNcode: String): Task[List[NovelBody]] =
      Task.raiseError(new UnsupportedOperationException)

    override def getNovelBody(aNcode: String, aPage: Int): Task[NovelBody] =
      Task.raiseError(new UnsupportedOperationException)

  }
}
