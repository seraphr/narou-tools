package jp.seraphr.narou.webui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import jp.seraphr.recharts.Tooltip.CursorStruct
import jp.seraphr.recharts._

import scala.scalajs.js

object RootView {
  case class Props(
    message: String
  )

  trait XYZ extends js.Object {
    val x: Int
    val y: Int
    val z: Int
  }

  private def data(aX: Int, aY: Int, aZ: Int): js.Any = {
    new XYZ {
      override val x: Int = aX
      override val y: Int = aY
      override val z: Int = aZ
    }
  }

  private val mData1 = js.Array(
    data(100, 200, 200),
    data(120, 100, 260),
    data(170, 300, 400),
    data(140, 250, 280),
    data(150, 400, 500),
    data(110, 280, 200)
  )
  private val mData2 = js.Array(
    data(200, 260, 240),
    data(240, 290, 220),
    data(190, 290, 250),
    data(198, 250, 210),
    data(180, 280, 260),
    data(210, 220, 230)
  )

  def apply(message: String): ScalaFnComponent.Unmounted[Props] = component(Props(message))

  val component =
    ScalaFnComponent[Props] { props =>
      React.Fragment(
        <.div(props.message),
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
            XAxis(XAxis.Props(aType = Axis.Type.number, aDataKey = "x", aName = "stature", aUnit = "cm")),
            YAxis(YAxis.Props(aDataKey = "y", aName = "weight", aUnit = "kg")),
            ZAxis(ZAxis.Props(aDataKey = "z", aRange = (64, 144), aName = "score", aUnit = "km")),
            Tooltip(Tooltip.Props(aCursor = CursorStruct("3 3"))),
            Legend(Legend.Props()),
            Scatter(Scatter.Props(aName = "A school", aData = mData1, aFill = "#8884d8"))(),
            Scatter(Scatter.Props(aName = "B school", aData = mData2, aFill = "#82ca9d"))()
          )
      )
    }

}
