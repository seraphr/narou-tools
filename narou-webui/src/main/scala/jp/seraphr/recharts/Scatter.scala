package jp.seraphr.recharts

import japgolly.scalajs.react.{ Children, CtorType, JsComponent }
import org.scalajs.dom.raw.SVGElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

object Scatter {
  @js.native
  @JSImport("recharts", "Scatter")
  object RawComponent extends js.Object

  // Curve実装するときにはそっちを使うようにする
  type CurveProps = js.Object
  type CurveType = String

  // Symbol実装するときはそっちを使うようにする
  type SymbolsProps = js.Object

  type LineType = String
  object LineType {
    val fitting = "fitting"
    val joint = "joint"
  }

  trait ScattterPointNode extends js.Object {
    val x: js.UndefOr[Int | String] = js.undefined
    val y: js.UndefOr[Int | String] = js.undefined
    val z: js.UndefOr[Int | String] = js.undefined
  }

  trait ScatterPointItem extends js.Object {
    val cx: js.UndefOr[Int] = js.undefined
    val cy: js.UndefOr[Int] = js.undefined
    val size: js.UndefOr[Int] = js.undefined
    val node: js.UndefOr[ScattterPointNode] = js.undefined
    val payload: js.UndefOr[js.Any] = js.undefined
  }

  // 元構造は PresentationAttributes<SVGElement> と & を取っている
  trait Props extends js.Object {
    val data: js.UndefOr[js.Array[js.Any]] = js.undefined

    val xAxisId: js.UndefOr[String | Int] = js.undefined
    val yAxisId: js.UndefOr[String | Int] = js.undefined
    val zAxisId: js.UndefOr[String | Int] = js.undefined

    val left: js.UndefOr[Int]
    val top: js.UndefOr[Int]

    //    yAxis?: Omit<XAxisProps, 'scale'> & { scale: D3Scale<string | number> };
    //    xAxis?: Omit<YAxisProps, 'scale'> & { scale: D3Scale<string | number> };
    //    zAxis?: Omit<ZAxisProps, 'scale'> & { scale: D3Scale<string | number> };

    val dataKey: js.UndefOr[DataKey[js.Any]] = js.undefined

    val line: js.UndefOr[ReactElement[SVGElement] | js.Function1[js.Any, SVGElement] | CurveProps | Boolean] = js.undefined
    val lineType: js.UndefOr[LineType] = js.undefined
    val lineJointType: js.UndefOr[CurveType] = js.undefined
    val legendType: js.UndefOr[LegendType] = js.undefined
    val tooltipType: js.UndefOr[TooltipType] = js.undefined
    val className: js.UndefOr[String] // これ定義だと ? ついてなかったけど、サンプルのコードは渡してなかった・・・
    val name: js.UndefOr[String | Int]

    val activeIndex: js.UndefOr[Int]
    val activeShape: js.UndefOr[ReactElement[SVGElement] | js.Function1[js.Any, SVGElement] | SymbolsProps]
    val shape: js.UndefOr[SymbolType | ReactElement[SVGElement] | js.Function1[js.Any, SVGElement]]
    val points: js.UndefOr[js.Array[ScatterPointItem]]
    val hide: js.UndefOr[Boolean]

    val isAnimationActive: js.UndefOr[Boolean]
    val animationId: js.UndefOr[Int]
    val animationBegin: js.UndefOr[Int]
    val animationDuration: js.UndefOr[Int]
    val animationEasing: js.UndefOr[AnimationTiming]
  }

  private val component = JsComponent[Props, Children.Varargs, Null](RawComponent)
  def apply(p: Props)(children: CtorType.ChildArg*): JsComponent.Unmounted[Props, Null] = component(p)(children: _*)
}
