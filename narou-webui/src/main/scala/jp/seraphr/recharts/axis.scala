package jp.seraphr.recharts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomNode
import org.scalajs.dom.raw.{ SVGElement, SVGLineElement, SVGTextElement }

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.{ UndefOr, | }

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
    def number(n: Int): AxisDomainItem = n
    val auto: AxisDomainItem = "auto"
    val dataMin: AxisDomainItem = "dataMin"
    val dataMax: AxisDomainItem = "dataMax"
  }
  type AxisDomain = js.Array[AxisDomainItem]
  object AxisDomain {
    def apply(aTuple: (AxisDomainItem, AxisDomainItem)): AxisDomain = js.Array(aTuple._1, aTuple._2)
  }

  type AxisInterval = Int | String
  object AxisInterval {
    val preserveStart: AxisInterval = "preserveStart"
    val preserveEnd: AxisInterval = "preserveEnd"
    val preserveStartEnd: AxisInterval = "preserveStartEnd"
  }

  type LabelType = String | Int | ReactElement[_] | js.Object
}

trait BaseAxisProps extends js.Object {
  import Axis._
  type ReactElement[E] = VdomNode
  type PresentationAttributes[E] = js.Object

  /** The type of axis */
  val `type`: js.UndefOr[Axis.Type] = js.undefined
  /** The key of data displayed in the axis */
  val dataKey: js.UndefOr[DataKey[js.Any]] = js.undefined
  /** Whether or not display the axis */
  val hide: js.UndefOr[Boolean] = js.undefined
  /** The scale type or functor of scale */
  val scale: js.UndefOr[Axis.ScaleType | js.Function] = js.undefined
  /** The option for tick */
  val tick: js.UndefOr[PresentationAttributes[SVGTextElement] | ReactElement[SVGElement] | ContentRenderer[_] | Boolean] = js.undefined
  /** The count of ticks */
  val tickCount: js.UndefOr[Int] = js.undefined
  /** The option for axisLine */
  val axisLine: js.UndefOr[Boolean | PresentationAttributes[SVGLineElement]] = js.undefined
  /** The option for tickLine */
  val tickLine: js.UndefOr[Boolean | PresentationAttributes[SVGTextElement]] = js.undefined
  /** The size of tick line */
  val tickSize: js.UndefOr[Int] = js.undefined
  /** The formatter function of tick */
  val tickFormatter: js.UndefOr[(js.Any, Int) => String] = js.undefined
  /**
   * When domain of the axis is specified and the type of the axis is "number",
   * if allowDataOverflow is set to be false,
   * the domain will be adjusted when the minimum value of data is smaller than domain[0] or
   * the maximum value of data is greater than domain[1] so that the axis displays all data values.
   * If set to true, graphic elements (line, area, bars) will be clipped to conform to the specified domain.
   */
  val allowDataOverflow: js.UndefOr[Boolean] = js.undefined
  /**
   * Allow the axis has duplicated categorys or not when the type of axis is "category".
   */
  val allowDuplicatedCategory: js.UndefOr[Boolean] = js.undefined
  /**
   * Allow the ticks of axis to be decimals or not.
   */
  val allowDecimals: js.UndefOr[Boolean] = js.undefined
  /** The domain of scale in this axis */
  val domain: js.UndefOr[Axis.AxisDomain] = js.undefined
  /** The name of data displayed in the axis */
  val name: js.UndefOr[String] = js.undefined
  /** The unit of data displayed in the axis */
  val unit: js.UndefOr[String | Int] = js.undefined
  /** The type of axis */
  val axisType: js.UndefOr[Axis.AxisType] = js.undefined
  val range: js.UndefOr[js.Array[Int]] = js.undefined
  /** axis react component */
  val AxisComp: js.UndefOr[js.Any] = js.undefined
  val label: js.UndefOr[LabelType] = js.undefined
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
    val xAxisId: js.UndefOr[String | Int] = js.undefined
    /** The width of axis which is usually calculated internally */
    val width: js.UndefOr[Int] = js.undefined
    /** The height of axis, which need to be setted by user */
    val height: js.UndefOr[Int] = js.undefined
    val mirror: js.UndefOr[Boolean] = js.undefined
    // The orientation of axis
    val orientation: js.UndefOr[Orientation] = js.undefined
    /**
     * Ticks can be any type when the axis is the type of category
     * Ticks must be Ints when the axis is the type of Int
     */
    val ticks: js.UndefOr[Array[String | Int]] = js.undefined
    val padding: js.UndefOr[Padding] = js.undefined
    val minTickGap: js.UndefOr[Int] = js.undefined
    val interval: js.UndefOr[AxisInterval] = js.undefined
    val reversed: js.UndefOr[Boolean] = js.undefined
  }

  object Props {
    def apply(
      aType: UndefOr[Type] = js.undefined,
      aDataKey: js.UndefOr[String | Int] = js.undefined,
      aName: js.UndefOr[String] = js.undefined,
      aUnit: js.UndefOr[String | Int] = js.undefined,
      aDomain: js.UndefOr[(AxisDomainItem, AxisDomainItem)] = js.undefined,
      aLabel: js.UndefOr[String] = js.undefined
    ): Props = {
      val tLabelObject = aLabel.map { tLabel =>
        js.Dictionary[js.Any](
          "value" -> tLabel,
          "angle" -> 0,
          "position" -> "insideBottom"
        ).asInstanceOf[js.Object]: LabelType
      }

      new Props {
        override val `type`: UndefOr[Type] = aType
        override val dataKey: UndefOr[DataKey[js.Any]] = aDataKey.map(a => a.asInstanceOf[DataKey[js.Any]])
        override val name: UndefOr[String] = aName
        override val unit: UndefOr[String | Int] = aUnit
        override val domain: UndefOr[AxisDomain] = aDomain.map(AxisDomain(_))
        override val label: UndefOr[LabelType] = tLabelObject
      }
    }
  }

  private val component = JsComponent[Props, Children.None, Null](RawComponent)
  def apply(p: Props): JsComponent.Unmounted[Props, Null] = component(p)
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
    val yAxisId: js.UndefOr[String | Int] = js.undefined
    /** The width of axis which is usually calculated internally */
    val width: js.UndefOr[Int] = js.undefined
    /** The height of axis, which need to be setted by user */
    val height: js.UndefOr[Int] = js.undefined
    val mirror: js.UndefOr[Boolean] = js.undefined
    // The orientation of axis
    val orientation: js.UndefOr[Orientation] = js.undefined
    /**
     * Ticks can be any type when the axis is the type of category
     * Ticks must be Ints when the axis is the type of Int
     */
    val ticks: js.UndefOr[Array[String | Int]] = js.undefined
    val padding: js.UndefOr[Padding] = js.undefined
    val minTickGap: js.UndefOr[Int] = js.undefined
    val interval: js.UndefOr[AxisInterval] = js.undefined
    val reversed: js.UndefOr[Boolean] = js.undefined
  }

  object Props {
    def apply(
      aType: UndefOr[Type] = js.undefined,
      aDataKey: js.UndefOr[String | Int] = js.undefined,
      aName: js.UndefOr[String] = js.undefined,
      aUnit: js.UndefOr[String | Int] = js.undefined,
      aDomain: js.UndefOr[(AxisDomainItem, AxisDomainItem)] = js.undefined,
      aLabel: js.UndefOr[String] = js.undefined
    ): Props = {

      val tLabelObject = aLabel.map { tLabel =>
        js.Dictionary[js.Any](
          "value" -> tLabel,
          "angle" -> -90,
          "position" -> "insideLeft"
        ).asInstanceOf[js.Object]: LabelType
      }

      new Props {
        override val `type`: UndefOr[Type] = aType
        override val dataKey: UndefOr[DataKey[js.Any]] = aDataKey.map(a => a.asInstanceOf[DataKey[js.Any]])
        override val name: UndefOr[String] = aName
        override val unit: UndefOr[String | Int] = aUnit
        override val domain: UndefOr[AxisDomain] = aDomain.map(AxisDomain(_))
        override val label: UndefOr[LabelType] = tLabelObject
      }
    }
  }

  private val component = JsComponent[Props, Children.None, Null](RawComponent)
  def apply(p: Props): JsComponent.Unmounted[Props, Null] = component(p)
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
    val `type`: js.UndefOr[Type] = js.undefined
    /** The name of data displayed in the axis */
    val name: js.UndefOr[String | Int] = js.undefined
    /** The unit of data displayed in the axis */
    val unit: js.UndefOr[String | Int] = js.undefined
    /** The unique id of z-axis */
    val zAxisId: js.UndefOr[String | Int] = js.undefined
    /** The key of data displayed in the axis */
    val dataKey: js.UndefOr[DataKey[js.Any]] = js.undefined
    /** The range of axis */
    val range: js.UndefOr[js.Array[Int]] = js.undefined
    val scale: js.UndefOr[ScaleType | js.Function] = js.undefined
  }

  object Props {
    def apply(
      aType: UndefOr[Type] = js.undefined,
      aDataKey: UndefOr[DataKey[js.Any]] = js.undefined,
      aRange: UndefOr[(Int, Int)] = js.undefined,
      aName: UndefOr[String | Int] = js.undefined,
      aUnit: js.UndefOr[String | Int] = js.undefined
    ): Props = {
      new Props {
        override val `type`: UndefOr[Type] = aType
        override val name: UndefOr[String | Int] = aName
        override val unit: UndefOr[String | Int] = aUnit
        override val range: UndefOr[js.Array[Int]] = aRange.map { case (v1, v2) => js.Array(v1, v2) }
        override val dataKey: UndefOr[DataKey[js.Any]] = aDataKey
      }
    }
  }

  private val component = JsComponent[Props, Children.None, Null](RawComponent)
  def apply(p: Props): JsComponent.Unmounted[Props, Null] = component(p)
}