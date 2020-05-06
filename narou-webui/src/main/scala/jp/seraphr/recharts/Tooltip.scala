package jp.seraphr.recharts

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.{ UndefOr, | }
import japgolly.scalajs.react._

object Tooltip {
  @js.native
  @JSImport("recharts", "Tooltip")
  object RawComponent extends js.Object

  trait CursorStruct extends js.Object {
    val strokeDasharray: String
    val stroke: js.UndefOr[String] = js.undefined
  }

  object CursorStruct {
    def apply(aStrokeDasharray: String, aStroke: js.UndefOr[String] = js.undefined): CursorStruct = {
      new CursorStruct {
        override val strokeDasharray: String = aStrokeDasharray
        override val stroke: UndefOr[String] = aStroke
      }
    }
  }

  type Cursor = Boolean | ReactElement[_] | CursorStruct

  trait Props extends js.Object {
    val cursor: js.UndefOr[Cursor] = js.undefined
  }

  object Props {
    def apply(aCursor: js.UndefOr[Cursor] = js.undefined): Props = {
      new Props {
        override val cursor: UndefOr[Cursor] = aCursor
      }
    }
  }

  private val component = JsComponent[Props, Children.None, Null](RawComponent)
  def apply(p: Props): JsComponent.Unmounted[Props, Null] = component(p)
}
