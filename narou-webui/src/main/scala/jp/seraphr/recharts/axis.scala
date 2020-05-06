package jp.seraphr.recharts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomNode
import org.scalajs.dom.raw.{ SVGElement, SVGLineElement, SVGTextElement }

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

object Axis {

  type Type = String
  object Type {
    val category = "category"
    val number = "number"
  }

  type ScaleType = String
  object ScaleType {
    val auto = "auto"
    val linear = "linear"
    val pow = "pow"
    val sqrt = "sqrt"
    val log = "log"
    val identity = "identity"
    val time = "time"
    val band = "band"
    val point = "point"
    val ordinal = "ordinal"
    val quantile = "quantile"
    val quantize = "quantize"
    val utc = "utc"
    val sequential = "sequential"
    val threshold = "threshold"
  }

  type AxisType = String
  object AxisType {
    val xAxis = "xAxis"
    val yAxis = "yAxis"
    val angleAxis = "angleAxis"
    val radiusAxis = "radiusAxis"
  }

  type AxisDomainItem = String | Int | js.Function
  object AxisDomainItem {
    val auto = "auto"
    val dataMin = "dataMin"
    val dataMax = "dataMax"
  }
  type AxisDomain = (AxisDomainItem, AxisDomainItem)

  type AxisInterval = Int | String
  object AxisInterval {
    val preserveStart: AxisInterval = "preserveStart"
    val preserveEnd: AxisInterval = "preserveEnd"
    val preserveStartEnd: AxisInterval = "preserveStartEnd"
  }

}

trait BaseAxisProps extends js.Object {
  type ReactElement[E] = VdomNode
  type PresentationAttributes[E] = js.Object

  /** The type of axis */
  val `type`: js.UndefOr[Axis.Type]
  /** The key of data displayed in the axis */
  val dataKey: js.UndefOr[DataKey[js.Any]]
  /** Whether or not display the axis */
  val hide: js.UndefOr[Boolean]
  /** The scale type or functor of scale */
  val scale: js.UndefOr[Axis.ScaleType | js.Function]
  /** The option for tick */
  val tick: js.UndefOr[PresentationAttributes[SVGTextElement] | ReactElement[SVGElement] | ContentRenderer[_] | Boolean]
  /** The count of ticks */
  val tickCount: js.UndefOr[Int]
  /** The option for axisLine */
  val axisLine: js.UndefOr[Boolean | PresentationAttributes[SVGLineElement]]
  /** The option for tickLine */
  val tickLine: js.UndefOr[Boolean | PresentationAttributes[SVGTextElement]]
  /** The size of tick line */
  val tickSize: js.UndefOr[Int]
  /** The formatter function of tick */
  val tickFormatter: js.UndefOr[(js.Any, Int) => String]
  /**
   * When domain of the axis is specified and the type of the axis is "number",
   * if allowDataOverflow is set to be false,
   * the domain will be adjusted when the minimum value of data is smaller than domain[0] or
   * the maximum value of data is greater than domain[1] so that the axis displays all data values.
   * If set to true, graphic elements (line, area, bars) will be clipped to conform to the specified domain.
   */
  val allowDataOverflow: js.UndefOr[Boolean]
  /**
   * Allow the axis has duplicated categorys or not when the type of axis is "category".
   */
  val allowDuplicatedCategory: js.UndefOr[Boolean]
  /**
   * Allow the ticks of axis to be decimals or not.
   */
  val allowDecimals: js.UndefOr[Boolean]
  /** The domain of scale in this axis */
  val domain: js.UndefOr[Axis.AxisDomain]
  /** The name of data displayed in the axis */
  val name: js.UndefOr[String]
  /** The unit of data displayed in the axis */
  val unit: js.UndefOr[String | Number]
  /** The type of axis */
  val axisType: js.UndefOr[Axis.AxisType]
  val range: js.UndefOr[js.Array[Int]]
  /** axis react component */
  val AxisComp: js.UndefOr[js.Any]
}

object XAxis {
  import Axis._

  @js.native
  @JSImport("recharts", "XAxis")
  object RawComponent extends js.Object

  type Orientation = String
  object Orientation {
    val top = "top"
    val botton = "botom"
  }

  trait Padding extends js.Object {
    val left: js.UndefOr[Int] = js.undefined
    val right: js.UndefOr[Int] = js.undefined
  }

  trait Props extends BaseAxisProps {
    /** The unique id of x-axis */
    val xAxisId: js.UndefOr[String | Int]
    /** The width of axis which is usually calculated internally */
    val width: js.UndefOr[Int]
    /** The height of axis, which need to be setted by user */
    val height: js.UndefOr[Int]
    val mirror: js.UndefOr[Boolean]
    // The orientation of axis
    val orientation: js.UndefOr[Orientation]
    /**
     * Ticks can be any type when the axis is the type of category
     * Ticks must be Ints when the axis is the type of Int
     */
    val ticks: js.UndefOr[Array[String | Int]]
    val padding: js.UndefOr[Padding]
    val minTickGap: js.UndefOr[Int]
    val interval: js.UndefOr[AxisInterval]
    val reversed: js.UndefOr[Boolean]
  }

  private val component = JsFnComponent[Props, Children.None](RawComponent)
  def apply(p: Props): JsFnComponent.Unmounted[Props] = component(p)
}

object YAxis {
  import Axis._

  @js.native
  @JSImport("recharts", "YAxis")
  object RawComponent extends js.Object

  type Orientation = String
  object Orientation {
    val left = "left"
    val right = "right"
  }

  trait Padding extends js.Object {
    val top: js.UndefOr[Int] = js.undefined
    val bottom: js.UndefOr[Int] = js.undefined
  }

  trait Props extends BaseAxisProps {
    /** The unique id of x-axis */
    val yAxisId: js.UndefOr[String | Int]
    /** The width of axis which is usually calculated internally */
    val width: js.UndefOr[Int]
    /** The height of axis, which need to be setted by user */
    val height: js.UndefOr[Int]
    val mirror: js.UndefOr[Boolean]
    // The orientation of axis
    val orientation: js.UndefOr[Orientation]
    /**
     * Ticks can be any type when the axis is the type of category
     * Ticks must be Ints when the axis is the type of Int
     */
    val ticks: js.UndefOr[Array[String | Int]]
    val padding: js.UndefOr[Padding]
    val minTickGap: js.UndefOr[Int]
    val interval: js.UndefOr[AxisInterval]
    val reversed: js.UndefOr[Boolean]
  }

  private val component = JsFnComponent[Props, Children.None](RawComponent)
  def apply(p: Props): JsFnComponent.Unmounted[Props] = component(p)
}

object ZAxis {
  import Axis._

  @js.native
  @JSImport("recharts", "ZAxis")
  object RawComponent extends js.Object

  type Orientation = String
  object Orientation {
    val left = "left"
    val right = "right"
  }

  trait Padding extends js.Object {
    val top: js.UndefOr[Int] = js.undefined
    val bottom: js.UndefOr[Int] = js.undefined
  }

  trait Props extends js.Object {
    val `type`: Type
    /** The name of data displayed in the axis */
    val name: js.UndefOr[String | Int]
    /** The unit of data displayed in the axis */
    val unit: js.UndefOr[String | Int]
    /** The unique id of z-axis */
    val zAxisId: js.UndefOr[String | Int]
    /** The key of data displayed in the axis */
    val dataKey: js.UndefOr[DataKey[js.Any]]
    /** The range of axis */
    val range: js.UndefOr[js.Array[Int]]
    val scale: js.UndefOr[ScaleType | js.Function]
  }

  private val component = JsFnComponent[Props, Children.None](RawComponent)
  def apply(p: Props): JsFnComponent.Unmounted[Props] = component(p)
}