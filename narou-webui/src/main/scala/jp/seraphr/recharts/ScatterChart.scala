package jp.seraphr.recharts

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react._

object ScatterChart {
  @js.native
  @JSImport("recharts", "ScatterChart")
  object RawComponent extends js.Object

  type Props = CategoricalChartProps
  val Props = CategoricalChartProps

  private val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(p: Props)(children: CtorType.ChildArg*): JsComponent.Unmounted[Props, Null] = component.apply(p)(children: _*)
}
