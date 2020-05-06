package jp.seraphr.recharts

import japgolly.scalajs.react.vdom.VdomNode

import scala.scalajs.js
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
}

trait BaseAxisProps extends js.Object {

  type DataKey[T] = String | Int | T => js.Any
  type SVGElement = VdomNode
  type SVGLineElement = VdomNode
  type SVGTextElement = VdomNode
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