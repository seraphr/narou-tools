package jp.seraphr.recharts

import japgolly.scalajs.react._
import typings.recharts.mod
import typings.recharts.generateCategoricalChartMod.CategoricalChartProps

object ScatterChart {

  type Props = CategoricalChartProps
  val Props = CategoricalChartProps

  private val component = JsComponent[Props, Children.Varargs, Null](mod.ScatterChart.^)

  def apply(p: Props)(children: CtorType.ChildArg*): JsComponent.Unmounted[Props, Null] = component.apply(p)(children: _*)
}
