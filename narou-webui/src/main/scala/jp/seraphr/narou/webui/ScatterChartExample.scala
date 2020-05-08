package jp.seraphr.narou.webui

import japgolly.scalajs.react._
import jp.seraphr.recharts.Tooltip.CursorStruct
import jp.seraphr.recharts._

import scala.scalajs.js
import scala.util.Random

object ScatterChartExample {
  trait XYZ extends js.Object {
    val x: Int
    val y: Int
    val z: Int
  }

  private def data(aX: Int, aY: Int, aZ: Int): XYZ = {
    new XYZ {
      override val x: Int = aX
      override val y: Int = aY
      override val z: Int = aZ
    }
  }

  private val mRandom = new Random(10)
  private def randomData(): XYZ = {
    val x = mRandom.nextInt(10000)
    val y = mRandom.nextInt(100000)
    val z = mRandom.nextInt(500)
    data(x, y, z)
  }

  private def randomDataSeq(aSize: Int): Seq[XYZ] = {
    Seq.fill(aSize)(randomData())
  }

  val mData1 = Seq(
    data(100, 200, 200),
    data(120, 100, 260),
    data(170, 300, 400),
    data(140, 250, 280),
    data(150, 400, 500),
    data(110, 280, 200)
  )
  val mData2 = Seq(
    data(200, 260, 240),
    data(240, 290, 220),
    data(190, 290, 250),
    data(198, 250, 210),
    data(180, 280, 260),
    data(210, 220, 230)
  )

  case class Props(dataCount: Int)

  val compolent = ScalaFnComponent[Props] { case Props(tDataCount) =>
    val tRandomData1 = randomDataSeq(tDataCount)
    val tRandomData2 = randomDataSeq(tDataCount)

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
        ZAxis(ZAxis.Props(aDataKey = "z", aRange = (10, 10), aName = "score", aUnit = "km")),
        Tooltip(Tooltip.Props(aCursor = CursorStruct("3 3"))),
        Legend(Legend.Props()),
        Scatter(Scatter.Props(aName = "A school", aData = tRandomData1, aFill = "#8884d8", aIsAnimationActive = false))(),
        Scatter(Scatter.Props(aName = "B school", aData = tRandomData2, aFill = "#82ca9d", aIsAnimationActive = false))()
      )
  }

  def apply(dataCount: Int): ScalaFnComponent.Unmounted[Props] = compolent(Props(dataCount))
}
