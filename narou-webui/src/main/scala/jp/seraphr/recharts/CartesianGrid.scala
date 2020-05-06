package jp.seraphr.recharts

import japgolly.scalajs.react.{ Children, JsComponent, vdom }
import org.scalajs.dom.raw.{ SVGElement, SVGLineElement }

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

object CartesianGrid {
  @js.native
  @JSImport("recharts", "CartesianGrid")
  object RawComponent extends js.Object

  type PresentationAttributes[E] = js.Object
  type ReactElement[E] = vdom.VdomElement
  type GridLineType = PresentationAttributes[SVGLineElement] | js.Function1[js.Any, SVGElement] | Boolean

  trait Props extends js.Object {
    val horizontal: js.UndefOr[GridLineType]
    val vertical: js.UndefOr[GridLineType]
    val horizontalPoints: js.UndefOr[js.Array[Int]]
    val verticalPoints: js.UndefOr[js.Array[Int]]
    val verticalFill: js.UndefOr[Array[String]]
    val horizontalFill: js.UndefOr[Array[String]]
  }

  object Props {
    def apply(dic: Map[String, js.Any]): Props = {
      import scala.scalajs.js.JSConverters._

      dic.toJSDictionary.asInstanceOf[Props]
    }
  }

  private val component = JsComponent[Props, Children.None, Null](RawComponent)
  def apply(p: Props): JsComponent.Unmounted[Props, Null] = component(p)
}
