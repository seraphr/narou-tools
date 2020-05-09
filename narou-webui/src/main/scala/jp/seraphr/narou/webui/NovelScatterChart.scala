package jp.seraphr.narou.webui

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.ScalaFnComponent
import jp.seraphr.narou.model.{ NarouNovel, NovelCondition }
import jp.seraphr.recharts.Axis.AxisDomainItem
import jp.seraphr.recharts.{ Axis, CartesianGrid, Legend, Margin, Scatter, ScatterChart, Tooltip, XAxis, YAxis, ZAxis }
import jp.seraphr.recharts.Tooltip.CursorStruct

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

case class AxisData(toValue: NarouNovel => Double, name: String, unit: String = "")
object AxisData {
  val bookmark = AxisData(_.bookmarkCount, "bookmark")
  val evaluationPoint = AxisData(_.evaluationPoint, "評価ポイント", "pt")
  val globalPoint = AxisData(_.globalPoint, "総合ポイント", "pt")
  val evaluationPerBookmark = AxisData(n => n.evaluationPoint * 1000 / n.bookmarkCount, "評価ポイント/ブックマーク")
}

case class ScatterData(condition: NovelCondition, color: String)

object NovelScatterChart {

  case class Props(novels: Seq[NarouNovel], axisX: AxisData, axisY: AxisData, scatters: Seq[ScatterData])

  @JSExportAll
  case class PointData(x: Double, y: Double, z: String)
  private def createPointData(aNovels: Seq[NarouNovel], aAxisX: AxisData, aAxisY: AxisData, aScatter: ScatterData): Seq[PointData] = {
    val tTargetNovels = aNovels.filter(aScatter.condition.predicate)

    tTargetNovels.map { n =>
      PointData(aAxisX.toValue(n), aAxisY.toValue(n), n.title)
    }
  }

  val compolent = ScalaFnComponent[Props] { case Props(aNovels, aAxisX, aAxisY, aScatters) =>
    val tNovels = aNovels.map(js.use(_).as[js.Any])
    println(s"========= novel size = ${tNovels.size}")

    val tScatters: Seq[ChildArg] = aScatters.map { tScatterData =>
      val tPoints = createPointData(aNovels, aAxisX, aAxisY, tScatterData).asInstanceOf[Seq[js.Any]]
      Scatter(Scatter.Props(aName = tScatterData.condition.name, aData = tPoints, aFill = tScatterData.color, aIsAnimationActive = false))()
    }

    val tChildren: Seq[ChildArg] = Seq(
      CartesianGrid(CartesianGrid.Props(Map(
        "strokeDasharray" -> "3 3"
      ))),
      XAxis(XAxis.Props(aType = Axis.Type.number, aDataKey = "x", aName = aAxisX.name, aUnit = aAxisX.unit)),
      YAxis(YAxis.Props(aDataKey = "y", aName = aAxisY.name, aUnit = aAxisY.unit, aDomain = (AxisDomainItem.number(0), AxisDomainItem.dataMax))),
      ZAxis(ZAxis.Props(aType = Axis.Type.category, aDataKey = "z", aRange = (15, 15), aName = "title")),
      Tooltip(Tooltip.Props(aCursor = CursorStruct("3 3"))),
      Legend(Legend.Props())
    )

    ScatterChart(
      ScatterChart.Props(
        aWidth = 730,
        aHeight = 250,
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
          ScatterData(NovelCondition.finished.withBookmark100, "red"),
          ScatterData(NovelCondition.finished.not.withBookmark100, "green")
        )
      )
    )
  }
}
