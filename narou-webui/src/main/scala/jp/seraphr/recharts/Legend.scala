package jp.seraphr.recharts

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react._

object Legend {
  @js.native
  @JSImport("recharts", "Legend")
  object RawComponent extends js.Object

  trait Props extends js.Object

  object Props {
    def apply(): Props = new Props {}
  }

  private val component = JsComponent[Props, Children.None, Null](RawComponent)
  def apply(p: Props): JsComponent.Unmounted[Props, Null] = component(p)
}
