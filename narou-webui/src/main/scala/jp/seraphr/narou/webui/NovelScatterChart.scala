package jp.seraphr.narou.webui

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.ScalaFnComponent
import japgolly.scalajs.react.raw.React.Element
import jp.seraphr.narou.model.{ NarouNovel, NovelCondition }
import jp.seraphr.recharts.{ Axis, CartesianGrid, ScatterChart }
import org.scalajs.dom.raw.SVGElement
import typings.react.mod.SVGProps
import typings.recharts.components.Scatter
import typings.recharts.rechartsStrings

import scala.scalajs.js.|
import typings.recharts.utilTypesMod.Margin

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.util.Random

case class AxisData(toValue: NarouNovel => Option[Int], name: String, unit: String = "")
object AxisData {
  implicit class ToOption[A](a: A) {
    def option: Option[A] = Option(a)
  }
  val bookmark = AxisData(_.bookmarkCount.option, "bookmark")
  val evaluationPoint = AxisData(_.evaluationPoint.option, "評価ポイント", "pt")
  val globalPoint = AxisData(_.globalPoint.option, "総合ポイント", "pt")
  val evaluationPerBookmark = AxisData(n => if (n.bookmarkCount == 0) None else (n.evaluationPoint * 1000 / n.bookmarkCount).option, "評価ポイント/ブックマーク")
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
    val top = RepresentativeData("最上位", vs => vs.max)
  }

  /** データを区間で区切って、代表値に最も近いものを残す */
  def representative(aCondition: Option[NovelCondition], aInterval: Int, aMinSectionCount: Int, aRepData: RepresentativeData, aColor: String): ScatterData = {
    def convert(aInput: ConvertInput): Seq[NarouNovel] = {
      val tBase = aInput.novels
      val tNovels = aCondition.fold(tBase)(c => tBase.filter(c.predicate))
      if (tNovels.isEmpty) return tNovels

      def xValue(n: NarouNovel) = aInput.x.toValue(n)
      def yValue(n: NarouNovel) = aInput.y.toValue(n)
      val tMinCount = 1 max aMinSectionCount

      tNovels.groupBy(xValue(_).map(_ / aInterval)).collect {
        case (Some(k), v) => k -> v
      }
        .toVector.sortBy(_._1).map(_._2) // 値で昇順にsortして
        .concat(Seq.fill(tMinCount - 1)(Seq.empty)) // 後ろにslidingに必要なだけ空列をつなげて
        .sliding(tMinCount) // slidingすることで、各グループごとにmapする
        .map { tNovelss =>
          // 最低数に達するまで、結合する = 足らない場合は移動平均にする
          tNovelss.foldLeft(Vector[NarouNovel]()) {
            case (tResult, _) if tMinCount <= tResult.size => tResult
            case (tResult, ns)                             => tResult ++ ns
          }
        }
        .flatMap {
          case tNovels =>
            // ここには、emptyなものは来ない
            val tRepValue = aRepData.value(tNovels.flatMap(yValue))
            val tRepNovel = tNovels.minBy { n =>
              // 代表値に最も近いものを返す
              yValue(n).fold(Int.MaxValue)(v => (v - tRepValue).abs)
            }

            Seq(tRepNovel)
        }.toVector
    }

    val tConditionName = aCondition.fold("")(c => s"${c.name}-")
    val tName = s"${tConditionName} ${aInterval}毎 ${aRepData.name}"
    ScatterData(tName, convert, aColor)
  }
}

object NovelScatterChart {
  import js.JSConverters._
  import jp.seraphr.recharts.Implicits._

  case class Props(novels: Seq[NarouNovel], axisX: AxisData, axisY: AxisData, scatters: Seq[ScatterData])

  @JSExportAll
  case class PointData(x: Double, y: Double, z: String)
  private def createPointData(aNovels: Seq[NarouNovel], aAxisX: AxisData, aAxisY: AxisData, aScatter: ScatterData): Seq[PointData] = {
    aScatter.convert(ConvertInput(aNovels, aAxisX, aAxisY)).flatMap { n =>
      for {
        x <- aAxisX.toValue(n)
        y <- aAxisY.toValue(n)
      } yield PointData(x, y, n.title)
    }
  }

  val compolent = ScalaFnComponent[Props] { case Props(aNovels, aAxisX, aAxisY, aScatters) =>
    val tScatters: Seq[ChildArg] = aScatters.map { tScatterData =>
      val tPoints = createPointData(aNovels, aAxisX, aAxisY, tScatterData).toJSArray
      val tName = s"${tScatterData.name}(${tPoints.size})"
      Scatter.create(tName, js.undefined)
        .data(tPoints)
        .fill(tScatterData.color)
        .isAnimationActive(false).build
    }

    import typings.recharts.components.{ XAxis, YAxis, ZAxis, Tooltip, Legend, Label }

    val tChildren: Seq[ChildArg] = Seq(
      CartesianGrid(CartesianGrid.Props().setStrokeDasharray("3 3")),
      XAxis
        .`type`(Axis.Type.number)
        .dataKey("x")
        .name(aAxisX.name)
        .label(
          Label.create()
            .value(aAxisX.name)
            .angle(0)
            .position(rechartsStrings.insideBottom)
            .build.rawElement
        )
        .unit(aAxisX.unit),
      YAxis.create()
        .dataKey("y")
        .name(aAxisY.name)
        .label(
          Label.create()
            .value(aAxisY.name)
            .angle(-90)
            .position(rechartsStrings.insideLeft)
            .build.rawElement
        )
        .unit(aAxisY.unit),
      ZAxis().
        `type`(rechartsStrings.category)
        .dataKey("z")
        .range(js.Array(10, 10))
        .name("title"),
      Tooltip.cursor(SVGProps[SVGElement]().setStrokeDasharray("3 3").asInstanceOf[Boolean | Element | SVGProps[SVGElement]]),
      Legend.create()
    )

    ScatterChart(
      ScatterChart.Props().setWidth(1200).setHeight(400).setMargin(
        Margin().setTop(20)
          .setRight(20)
          .setBottom(10)
          .setLeft(10)
      )
    )(tChildren ++ tScatters: _*)
  }

  def apply(aNovels: Seq[NarouNovel], aScatters: Seq[ScatterData]): ScalaFnComponent.Unmounted[Props] = {
    compolent(
      Props(
        novels = aNovels,
        axisX = AxisData.bookmark,
        axisY = AxisData.evaluationPerBookmark,
        scatters = aScatters
      )
    )
  }
}
