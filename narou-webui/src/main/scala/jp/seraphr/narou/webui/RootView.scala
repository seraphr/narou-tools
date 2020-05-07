package jp.seraphr.narou.webui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

object RootView {
  @nowarn("cat=unused")
  private val css = CSS

  case class Props(
    message: String
  )

  case class State(dataCount: Int)

  def apply(message: String) = component(Props(message))

  val component =
    ScalaComponent.builder[Props]("RootView")
      .initialState(State(dataCount = 100))
      .renderPS { case (scope, p, s) =>
        import typings.antd.components._

        val dataCountState = scope.mountedPure.zoomState(_.dataCount)(c => s => s.copy(dataCount = c))
        React.Fragment(
          <.div(p.message),
          InputNumber(
            defaultValue = s.dataCount,
            onChange = { v => dataCountState.setStateOption(v.toOption.map(_.toInt)) }
          )(),
          ScatterChartExample(s.dataCount)
        )
      }
      .build

}
