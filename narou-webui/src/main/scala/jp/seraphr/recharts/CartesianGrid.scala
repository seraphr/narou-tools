package jp.seraphr.recharts

import japgolly.scalajs.react.{ Children, JsComponent }
import typings.recharts.cartesianGridMod
import typings.recharts.mod

object CartesianGrid {
  type Props = cartesianGridMod.Props
  val Props                                               = cartesianGridMod.Props
  private val component                                   = JsComponent[Props, Children.None, Null](mod.CartesianGrid.^)
  def apply(p: Props): JsComponent.Unmounted[Props, Null] = component(p)
}
