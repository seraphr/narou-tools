package jp.seraphr.narou.webui

import jp.seraphr.narou.model.{ Genre, NarouNovel, NovelCondition, NovelType, UploadType }
import jp.seraphr.narou.webui.ScatterData.RangeFilter
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ScatterDataTest extends AnyFreeSpec with Matchers {
  private val mXAxis = AxisData(tNovel => Some(tNovel.bookmarkCount), "x")
  private val mYAxis = AxisData(tNovel => Some(tNovel.evaluationPoint), "y")

  private def createNovel(
      aNcode: String,
      aXValue: Int,
      aYValue: Int,
      aIsFinished: Boolean = true
  ): NarouNovel =
    NarouNovel(
      title = aNcode,
      ncode = aNcode,
      userId = "userId",
      writer = "writer",
      story = "story",
      genre = Genre.values.head,
      gensaku = "",
      keywords = Seq.empty,
      firstUpload = "firstUpload",
      lastUpload = "lastUpload",
      novelType = NovelType.Serially,
      isFinished = aIsFinished,
      chapterCount = 1,
      length = 1,
      readTimeMinutes = 1,
      isR15 = false,
      isBL = false,
      isGL = false,
      isZankoku = false,
      isTensei = false,
      isTenni = false,
      uploadType = UploadType.PC,
      globalPoint = aXValue + aYValue,
      bookmarkCount = aXValue,
      reviewCount = 0,
      evaluationPoint = aYValue,
      evaluationCount = 0,
      illustrationCount = 0,
      novelUpdatedAt = "novelUpdatedAt",
      updatedAt = "updatedAt"
    )

  private def extract(
      aNovels: Seq[NarouNovel],
      aWindow: Int,
      aRangeFilter: RangeFilter,
      aCondition: Option[NovelCondition] = None,
      aXAxis: AxisData = mXAxis,
      aYAxis: AxisData = mYAxis
  ): Seq[NarouNovel] = {
    val tScatterData = ScatterData.range(aCondition, aWindow, aRangeFilter, "blue")
    tScatterData.convert(ConvertInput(aNovels, aXAxis, aYAxis))
  }

  private val mAllValues = RangeFilter("全件", (_, _) => true)

  "range" - {
    "入力順が異なる時、抽出すると、x軸値とncodeの昇順で同じ結果になること" in {
      val tNovels = Seq(
        createNovel("N003", 2, 30),
        createNovel("N002", 1, 20),
        createNovel("N001", 1, 10),
        createNovel("N004", 3, 40)
      )

      val tExpected = Seq("N001", "N002", "N003", "N004")
      extract(tNovels, 3, mAllValues).map(_.ncode) shouldBe tExpected
      extract(tNovels.reverse, 3, mAllValues).map(_.ncode) shouldBe tExpected
    }

    "対象が先頭・中央・末尾にある時、抽出すると、範囲内へずらした局所ウィンドウを使うこと" in {
      val tNovels = (1 to 5).map(tIndex => createNovel(f"N$tIndex%03d", tIndex, tIndex * 10))
      val tExpectedWindows = Map(
        10 -> Seq(10, 20, 30),
        30 -> Seq(20, 30, 40),
        50 -> Seq(30, 40, 50)
      )
      val tFilter = RangeFilter(
        "ウィンドウ検証",
        (tValues, tValue) => tExpectedWindows.get(tValue).contains(tValues)
      )

      extract(tNovels, 3, tFilter).map(_.ncode) shouldBe Seq("N001", "N003", "N005")
    }

    "対象件数がウィンドウ幅未満である時、抽出すると、全対象を比較対象に使うこと" in {
      val tNovels = (1 to 3).map(tIndex => createNovel(f"N$tIndex%03d", tIndex, tIndex * 10))
      val tFilter = RangeFilter(
        "ウィンドウ検証",
        (tValues, _) => tValues == Seq(10, 20, 30)
      )

      extract(tNovels, 10, tFilter).map(_.ncode) shouldBe Seq("N001", "N002", "N003")
    }

    "同じx軸値を持つ対象が入力順と異なるncode順である時、抽出すると、ncode順のウィンドウを使うこと" in {
      val tNovels = Seq(
        createNovel("N005", 1, 50),
        createNovel("N004", 1, 40),
        createNovel("N003", 1, 30),
        createNovel("N002", 1, 20),
        createNovel("N001", 1, 10)
      )
      val tFilter = RangeFilter(
        "ウィンドウ検証",
        (tValues, tValue) => tValue == 30 && tValues == Seq(20, 30, 40)
      )

      extract(tNovels, 3, tFilter).map(_.ncode) shouldBe Seq("N003")
    }

    "条件外またはx軸値・y軸値が欠損している小説がある時、抽出すると、結果にも比較対象にも含めないこと" in {
      val tNovels = Seq(
        createNovel("N001", 1, 10),
        createNovel("N002-missing-x", 2, 20),
        createNovel("N003-missing-y", 3, 30),
        createNovel("N004-filtered", 4, 40, aIsFinished = false)
      )
      val tXAxis = AxisData(
        tNovel => Option.when(tNovel.ncode != "N002-missing-x")(tNovel.bookmarkCount),
        "x"
      )
      val tYAxis = AxisData(
        tNovel => Option.when(tNovel.ncode != "N003-missing-y")(tNovel.evaluationPoint),
        "y"
      )
      val tFilter = RangeFilter("比較対象検証", (tValues, _) => tValues == Seq(10))

      extract(
        tNovels,
        4,
        tFilter,
        Some(NovelCondition.finished),
        tXAxis,
        tYAxis
      ).map(_.ncode) shouldBe Seq("N001")
    }

    "ウィンドウ幅が0以下である時、外れ値系列を作成すると、設定を拒否すること" in {
      an[IllegalArgumentException] should be thrownBy ScatterData.range(None, 0, mAllValues, "blue")
      an[IllegalArgumentException] should be thrownBy ScatterData.range(None, -1, mAllValues, "blue")
    }
  }

  "iqrBaseOutlier" - {
    "x軸順の局所ウィンドウに上側外れ値がある時、抽出すると、その値だけを返すこと" in {
      val tYValues = Seq(100, 0, 0, 0, 0, 100, 100, 100, 100)
      val tNovels  = tYValues.zipWithIndex.map { case (tYValue, i) =>
        createNovel(f"N${i + 1}%03d", i + 1, tYValue)
      }.reverse

      extract(tNovels, 5, RangeFilter.iqrBaseOutlier(true, 1.5)).map(_.ncode) shouldBe Seq("N001")
    }

    "x軸順の局所ウィンドウに下側外れ値がある時、抽出すると、その値だけを返すこと" in {
      val tYValues = Seq(-100, 0, 0, 0, 0, -100, -100, -100, -100)
      val tNovels  = tYValues.zipWithIndex.map { case (tYValue, i) =>
        createNovel(f"N${i + 1}%03d", i + 1, tYValue)
      }.reverse

      extract(tNovels, 5, RangeFilter.iqrBaseOutlier(false, 1.5)).map(_.ncode) shouldBe Seq("N001")
    }

    "値が上側または下側fenceと同値である時、判定すると、外れ値に含めないこと" in {
      val tUpper = RangeFilter.iqrBaseOutlier(true, 1).filter
      val tLower = RangeFilter.iqrBaseOutlier(false, 1).filter

      tUpper(Seq(0, 1, 2, 3), 5) shouldBe false
      tUpper(Seq(0, 1, 2, 3), 6) shouldBe true
      tLower(Seq(0, 1, 2, 3), -1) shouldBe false
      tLower(Seq(0, 1, 2, 3), -2) shouldBe true
    }

    "IQRが0ではなくfactorが1以外である時、下側を判定すると、factorとIQRの積からfenceを算出すること" in {
      val tLower = RangeFilter.iqrBaseOutlier(false, 1.5).filter

      tLower(Seq(0, 0, 1, 1), -2) shouldBe true
      tLower(Seq(0, 0, 1, 1), -1) shouldBe false
    }

    "IQRが0である時、判定すると、四分位値より厳密に外側の値だけを返すこと" in {
      val tUpper = RangeFilter.iqrBaseOutlier(true, 1.5).filter
      val tLower = RangeFilter.iqrBaseOutlier(false, 1.5).filter

      tUpper(Seq(1, 1, 1, 1, 100), 100) shouldBe true
      tUpper(Seq(1, 1, 1, 1, 100), 1) shouldBe false
      tLower(Seq(-100, 1, 1, 1, 1), -100) shouldBe true
      tLower(Seq(-100, 1, 1, 1, 1), 1) shouldBe false
    }

    "factorが負数またはNaNである時、外れ値系列を作成すると、設定を拒否すること" in {
      an[IllegalArgumentException] should be thrownBy RangeFilter.iqrBaseOutlier(true, -0.1)
      an[IllegalArgumentException] should be thrownBy RangeFilter.iqrBaseOutlier(false, Double.NaN)
    }
  }
}
