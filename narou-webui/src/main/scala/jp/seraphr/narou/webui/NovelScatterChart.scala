package jp.seraphr.narou.webui

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.ScalaFnComponent
import jp.seraphr.narou.model.{ NarouNovel, NovelCondition }
import jp.seraphr.recharts.{ Axis, CartesianGrid, Legend, Margin, Scatter, ScatterChart, Tooltip, XAxis, YAxis, ZAxis }
import jp.seraphr.recharts.Tooltip.CursorStruct

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.util.Random

case class AxisData(toValue: NarouNovel => Int, name: String, unit: String = "")
object AxisData {
  val bookmark = AxisData(_.bookmarkCount, "bookmark")
  val evaluationPoint = AxisData(_.evaluationPoint, "評価ポイント", "pt")
  val globalPoint = AxisData(_.globalPoint, "総合ポイント", "pt")
  val evaluationPerBookmark = AxisData(n => n.evaluationPoint * 1000 / n.bookmarkCount, "評価ポイント/ブックマーク")
}

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
  private val mRandom = new Random(1234)

  def filterAndSampling(aCondition: NovelCondition, aColor: String, aSampling: Sampling = Sampling.nop): ScatterData = {
    def convert(aInput: ConvertInput): Seq[NarouNovel] = {
      val tTargetNovels = aInput.novels.filter(aCondition.predicate)
      val tTargetSize = tTargetNovels.size
      val tSamplingCount = aSampling.calc(tTargetSize)

      if (tTargetSize == tSamplingCount) tTargetNovels
      else {
        val tRate = tSamplingCount.toDouble / tTargetSize
        tTargetNovels.filter { _ =>
          mRandom.nextDouble() <= tRate
        }
      }
    }

    ScatterData(aCondition.name, convert, aColor)
  }

  case class SectionData(name: String, value: NarouNovel => Int, interval: Int)
  case class RepresentativeData(name: String, value: Seq[Int] => Int)
  object RepresentativeData {
    val average = RepresentativeData("平均", vs => vs.sum / vs.size)
    val mean = RepresentativeData("中央値", vs => vs.sorted.apply(vs.size / 2))
  }

  /** データを区間で区切って、代表値に最も近いものを残す */
  def representative(aCondition: Option[NovelCondition], aInterval: Int, aRepData: RepresentativeData, aColor: String): ScatterData = {
    def convert(aInput: ConvertInput): Seq[NarouNovel] = {
      val tBase = aInput.novels
      val tNovels = aCondition.fold(tBase)(c => tBase.filter(c.predicate))
      def xValue(n: NarouNovel) = aInput.x.toValue(n)
      def yValue(n: NarouNovel) = aInput.y.toValue(n)

      tNovels.groupBy(xValue(_) / aInterval).flatMap {
        case (_, tNovels) if tNovels.isEmpty => Seq()
        case (_, tNovels) =>
          val tRepValue = aRepData.value(tNovels.map(yValue))
          val tRepNovel = tNovels.minBy { n =>
            // 代表値に最も近いものを返す
            (yValue(n) - tRepValue).abs
          }

          Seq(tRepNovel)
      }.toSeq
    }

    val tConditionName = aCondition.fold("")(c => s"${c.name}-")
    val tName = s"${tConditionName} ${aInterval}毎 ${aRepData.name}"
    ScatterData(tName, convert, aColor)
  }
}

object NovelScatterChart {
  case class Props(novels: Seq[NarouNovel], axisX: AxisData, axisY: AxisData, scatters: Seq[ScatterData])

  @JSExportAll
  case class PointData(x: Double, y: Double, z: String)
  private def createPointData(aNovels: Seq[NarouNovel], aAxisX: AxisData, aAxisY: AxisData, aScatter: ScatterData): Seq[PointData] = {
    aScatter.convert(ConvertInput(aNovels, aAxisX, aAxisY)).map { n =>
      PointData(aAxisX.toValue(n), aAxisY.toValue(n), n.title)
    }
  }

  val compolent = ScalaFnComponent[Props] { case Props(aNovels, aAxisX, aAxisY, aScatters) =>
    val tNovels = aNovels.map(js.use(_).as[js.Any])
    println(s"========= novel size = ${tNovels.size}")

    val tScatters: Seq[ChildArg] = aScatters.map { tScatterData =>
      val tPoints = createPointData(aNovels, aAxisX, aAxisY, tScatterData).asInstanceOf[Seq[js.Any]]
      val tName = s"${tScatterData.name}(${tPoints.size})"
      Scatter(Scatter.Props(aName = tName, aData = tPoints, aFill = tScatterData.color, aIsAnimationActive = false))()
    }

    val tChildren: Seq[ChildArg] = Seq(
      CartesianGrid(CartesianGrid.Props(Map(
        "strokeDasharray" -> "3 3"
      ))),
      XAxis(XAxis.Props(aType = Axis.Type.number, aDataKey = "x", aName = aAxisX.name, aLabel = aAxisX.name, aUnit = aAxisX.unit)),
      YAxis(YAxis.Props(aDataKey = "y", aName = aAxisY.name, aLabel = aAxisY.name, aUnit = aAxisY.unit)),
      ZAxis(ZAxis.Props(aType = Axis.Type.category, aDataKey = "z", aRange = (10, 10), aName = "title")),
      Tooltip(Tooltip.Props(aCursor = CursorStruct("3 3"))),
      Legend(Legend.Props())
    )

    ScatterChart(
      ScatterChart.Props(
        aWidth = 1920,
        aHeight = 500,
        aMargin = Margin(
          aTop = 20,
          aRight = 20,
          aBottom = 10,
          aLeft = 10
        )
      )
    )(tChildren ++ tScatters: _*)
  }

  implicit class CondOps(c: NovelCondition) {
    def withBookmark100 = c and NovelCondition.bookmark100
  }

  def apply(aNovels: Seq[NarouNovel]): ScalaFnComponent.Unmounted[Props] = {
    compolent(
      Props(
        novels = aNovels,
        axisX = AxisData.bookmark,
        axisY = AxisData.evaluationPerBookmark,
        scatters = Seq(
          ScatterData.filterAndSampling(NovelCondition.finished.withBookmark100, "red", Sampling.targetCount(1000)),
          ScatterData.filterAndSampling(NovelCondition.finished.not.withBookmark100, "green", Sampling.targetCount(1000))
        )
      )
    )
  }
}
