package jp.seraphr.narou.webui

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.ScalaFnComponent
import japgolly.scalajs.react.raw.React.Element
import jp.seraphr.narou.model.{ NarouNovel }
import jp.seraphr.recharts.{ Axis, CartesianGrid, ScatterChart }
import org.scalajs.dom.raw.SVGElement
import typings.react.mod.SVGProps
import typings.recharts.components.Scatter
import typings.recharts.rechartsStrings

import scala.scalajs.js.|
import typings.recharts.utilTypesMod.Margin

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

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

  val component = ScalaFnComponent[Props] { case Props(aNovels, aAxisX, aAxisY, aScatters) =>
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
    component(
      Props(
        novels = aNovels,
        axisX = AxisData.bookmark,
        axisY = AxisData.evaluationPerBookmark,
        scatters = aScatters
      )
    )
  }
}
