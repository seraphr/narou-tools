package jp.seraphr.narou.webui

import scala.util.Random

import jp.seraphr.narou.model.{ NarouNovel, NovelCondition }

case class Sampling(calc: Int => Int)

object Sampling {
  val nop = Sampling(identity)

  /** count個前後のデータ数になるようにサンプリング */
  def targetCount(count: Int) = Sampling(_ => count)

  /** 元の個数の rate倍になるようにサンプリング */
  def rateSampling(rate: Double) = Sampling(n => (n * rate).toInt)

  /** 元の数の平方根になるようにサンプリング */
  val sqrtSampling = Sampling(math.sqrt(_).toInt)
}

case class ConvertInput(novels: Seq[NarouNovel], x: AxisData, y: AxisData)
case class ScatterData(name: String, convert: ConvertInput => Seq[NarouNovel], color: String)
object ScatterData {
  private val mRandom                                                                            = new Random(1234)
  def memorizeConvert(convert: ConvertInput => Seq[NarouNovel]): ConvertInput => Seq[NarouNovel] = {
    var tMemorizedInput: ConvertInput     = null
    var tMemorizedResult: Seq[NarouNovel] = null
    tInput => {
      if (tMemorizedResult != null && tMemorizedInput == tInput) {
        tMemorizedResult
      } else {
        tMemorizedInput = tInput
        tMemorizedResult = convert(tInput)
        tMemorizedResult
      }
    }
  }

  def filterAndSampling(aCondition: NovelCondition, aColor: String, aSampling: Sampling = Sampling.nop): ScatterData = {
    def convert(aInput: ConvertInput): Seq[NarouNovel] = {
      val tTargetNovels  = aInput.novels.filter(aCondition.predicate)
      val tTargetSize    = tTargetNovels.size
      val tSamplingCount = aSampling.calc(tTargetSize)

      if (tTargetSize == tSamplingCount) tTargetNovels
      else {
        val tRate = tSamplingCount.toDouble / tTargetSize
        tTargetNovels.filter { _ =>
          mRandom.nextDouble() <= tRate
        }
      }
    }

    ScatterData(aCondition.name, memorizeConvert(convert), aColor)
  }

  case class SectionData(name: String, value: NarouNovel => Int, interval: Int)
  case class RepresentativeData(name: String, value: Seq[Int] => Int)
  object RepresentativeData {
    val average = RepresentativeData("平均", vs => vs.sum / vs.size)
    val mean    = RepresentativeData("中央値", vs => vs.sorted.apply(vs.size / 2))
    val top     = RepresentativeData("最上位", vs => vs.max)
  }

  case class RangeFilter(name: String, filter: (Seq[Int], Int) => Boolean)
  object RangeFilter {
    def percentile(name: String, min: Int, max: Int): RangeFilter = {
      def filter(from: Seq[Int], value: Int): Boolean = {
        val size                     = from.size
        def index(percent: Int): Int = {
          math.min(size - 1, math.max(0, (0.01 * size * percent).toInt))
        }
        val minValue                 = from(index(min))
        val maxValue                 = from(index(max))

        minValue <= value && value <= maxValue
      }

      RangeFilter(name, filter)
    }

    /**
     * 四分位範囲(q3 - q1)を用いたハズレ値を抽出する
     *
     * @param upper Trueだったら値が大きい側のハズレ値を残す
     * @param factor 四分位範囲の何倍をハズレ値との境とするかを決める係数。 大きいほど外れ値と判定される物の数が減る
     * @return
     */
    def iqrBaseOutlier(
        upper: Boolean,
        factor: Double
    ): RangeFilter = {
      def filter(from: Seq[Int], value: Int): Boolean = {
        val tSize    = from.size
        val tSorted  = from.sorted
        val tQ1Value = tSorted((tSize * 0.25).toInt)
        val tQ3Value = tSorted((tSize * 0.75).toInt)
        val tIqr     = tQ3Value - tQ1Value
        if (upper) {
          val tUpper = tQ3Value + tIqr * factor
          tUpper <= value
        } else {
          val tLower = tQ1Value - tIqr - factor
          value <= tLower
        }
      }

      val tName = f"外れ値(factor=$factor%1.1f)"
      RangeFilter(tName, filter)
    }

    val q0q1            = percentile("Q0-Q1", 0, 25)
    val q1q2            = percentile("Q1-Q2", 25, 50)
    val q2q3            = percentile("Q2-Q3", 50, 75)
    val q3q4            = percentile("Q3-Q4", 75, 100)
    val iqrUpperOutlier = iqrBaseOutlier(true, 1.5)
    val iqrLowerOutlier = iqrBaseOutlier(false, 1.5)
  }

  /** データを区間で区切って、代表値に最も近いものを残す */
  def representative(
      aCondition: Option[NovelCondition],
      aInterval: Int,
      aMinSectionCount: Int,
      aRepData: RepresentativeData,
      aColor: String
  ): ScatterData = {
    def convert(aInput: ConvertInput): Seq[NarouNovel] = {
      val tBase   = aInput.novels
      val tNovels = aCondition.fold(tBase)(c => tBase.filter(c.predicate))
      if (tNovels.isEmpty) return tNovels

      def xValue(n: NarouNovel) = aInput.x.toValue(n)
      def yValue(n: NarouNovel) = aInput.y.toValue(n)
      val tMinCount             = 1 max aMinSectionCount

      tNovels
        .groupBy(xValue(_).map(_ / aInterval))
        .collect { case (Some(k), v) =>
          k -> v
        }
        .toVector
        .sortBy(_._1)
        .map(_._2)                                  // 値で昇順にsortして
        .concat(Seq.fill(tMinCount - 1)(Seq.empty)) // 後ろにslidingに必要なだけ空列をつなげて
        .sliding(tMinCount)                         // slidingすることで、各グループごとにmapする
        .map { tNovelss =>
          // 最低数に達するまで、結合する = 足らない場合は移動平均にする
          tNovelss.foldLeft(Vector[NarouNovel]()) {
            case (tResult, _) if tMinCount <= tResult.size => tResult
            case (tResult, ns)                             => tResult ++ ns
          }
        }
        .flatMap { case tNovels =>
          // ここには、emptyなものは来ない
          val tRepValue = aRepData.value(tNovels.flatMap(yValue))
          val tRepNovel = tNovels.minBy { n =>
            // 代表値に最も近いものを返す
            yValue(n).fold(Int.MaxValue)(v => (v - tRepValue).abs)
          }

          Seq(tRepNovel)
        }
        .toVector
    }

    val tConditionName = aCondition.fold("")(c => s"${c.name}-")
    val tName          = s"${tConditionName} ${aInterval}毎 ${aRepData.name}"
    ScatterData(tName, memorizeConvert(convert), aColor)
  }

  /**
   * 各小説をyAxixの昇順に並べ、自身を中心とするWindow幅の小説がある範囲に含まれていた場合に残す
   *
   * @param aCondition
   * @param aWindow
   * @param aRange
   * @param aColor
   * @return
   */
  def range(aCondition: Option[NovelCondition], aWindow: Int, aRange: RangeFilter, aColor: String): ScatterData = {
    case class NovelWithValue(novel: NarouNovel, xValue: Int, index: Int)
    def convert(aInput: ConvertInput): Seq[NarouNovel] = {
      val tBase   = aInput.novels
      val tNovels = aCondition.fold(tBase)(c => tBase.filter(c.predicate))
      if (tNovels.isEmpty) return tNovels

      def xValue(n: NarouNovel) = aInput.x.toValue(n)
      def yValue(n: NarouNovel) = aInput.y.toValue(n)

      val tNovelWithValues = tNovels
        .view
        .map { n =>
          (n, xValue(n))
        }
        .zipWithIndex
        .collect { case ((n, Some(xValue)), i) =>
          NovelWithValue(n, xValue, i)
        }
        .toIndexedSeq

      val tSize = tNovelWithValues.size
      (0 until tSize)
        .iterator
        .map { // 各小説毎に、所属するグループ
          case i if i < aWindow / 2           =>
            // 前方に十分な数がないときは、先頭からwindowサイズだけ取る
            (tNovelWithValues.take(aWindow), tNovelWithValues(i))
          case i if (tSize - aWindow / 2) < i =>
            // 後方に十分な数がないときは、末尾からwindowサイズだけ取る
            (tNovelWithValues.takeRight(aWindow), tNovelWithValues(i))
          case i                              =>
            // 通常は、対象の小説を中心に、前後 (aWindow / 2)をとる
            (tNovelWithValues.drop(i - aWindow / 2).take(aWindow), tNovelWithValues(i))
        }
        .filter { case (ns, n) =>
          val values = ns.flatMap(n => yValue(n.novel))
          val value  = yValue(n.novel)
          value.fold(false)(aRange.filter(values, _))
        }
        .map(_._2.novel)
        .toVector
    }

    val tConditionName = aCondition.fold("")(c => s"${c.name}-")
    val tName          = s"${tConditionName} ${aRange.name}"
    ScatterData(tName, memorizeConvert(convert), aColor)
  }

}
