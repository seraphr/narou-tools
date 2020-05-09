package jp.seraphr.narou.webui

import japgolly.scalajs.react.ScalaFnComponent
import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.recharts.{ Axis, CartesianGrid, Legend, Margin, Scatter, ScatterChart, Tooltip, XAxis, YAxis, ZAxis }
import jp.seraphr.recharts.Tooltip.CursorStruct

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport }

class Hoge(field1: Int, field2: String) {
  @JSExport("hogehoge")
  val f1 = field1
  @JSExport("fugafuga")
  val f2 = field2
}

@js.native
trait HogeObj extends js.Object {
  def hogehoge: Int = js.native
  def fugafuga: String = js.native
}

object NovelScatterChart {
  case class Props(novels: Seq[NarouNovel])

  val compolent = ScalaFnComponent[Props] { case Props(aNovels) =>
    val tNovels = aNovels.map(js.use(_).as[js.Any])
    println(s"========= novel size = ${tNovels.size}")

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
    )(
        CartesianGrid(CartesianGrid.Props(Map(
          "strokeDasharray" -> "3 3"
        ))),
        XAxis(XAxis.Props(aType = Axis.Type.number, aDataKey = "bookmarkCount", aName = "bookmark")),
        YAxis(YAxis.Props(aDataKey = "globalPoint", aName = "評価", aUnit = "pt")),
        ZAxis(ZAxis.Props(aType = Axis.Type.category, aDataKey = "title", aRange = (20, 20), aName = "title", aUnit = "")),
        Tooltip(Tooltip.Props(aCursor = CursorStruct("3 3"))),
        Legend(Legend.Props()),
        Scatter(Scatter.Props(aName = "narou", aData = tNovels, aFill = "#8884d8", aIsAnimationActive = false))()
      )
  }

  def apply(aNovels: Seq[NarouNovel]): ScalaFnComponent.Unmounted[Props] = compolent(Props(aNovels))
}
