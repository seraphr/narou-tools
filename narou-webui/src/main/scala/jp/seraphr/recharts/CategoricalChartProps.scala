package jp.seraphr.recharts

import scala.scalajs.js
import scala.scalajs.js.UndefOr

trait Margin extends js.Object {
  val top: js.UndefOr[Int] = js.undefined
  val right: js.UndefOr[Int] = js.undefined
  val bottom: js.UndefOr[Int] = js.undefined
  val left: js.UndefOr[Int] = js.undefined
}

object Margin {
  def apply(
    aTop: Option[Int] = None,
    aRight: Option[Int] = None,
    aBottom: Option[Int] = None,
    aLeft: Option[Int] = None
  ): Margin = {
    import jp.seraphr.js.ScalaJsConverters._

    new Margin {
      override val top: UndefOr[Int] = aTop.asUndefOr
      override val right: UndefOr[Int] = aRight.asUndefOr
      override val bottom: UndefOr[Int] = aBottom.asUndefOr
      override val left: UndefOr[Int] = aLeft.asUndefOr
    }
  }
}

trait CategoricalChartProps extends js.Object {
  val width: js.UndefOr[Int] = js.undefined
  val height: js.UndefOr[Int] = js.undefined
  val margin: js.UndefOr[Margin] = js.undefined
}

object CategoricalChartProps {
  def apply(
    aWidth: Option[Int] = None,
    aHeight: Option[Int] = None,
    aMargin: Option[Margin] = None
  ): CategoricalChartProps = {
    import jp.seraphr.js.ScalaJsConverters._

    new CategoricalChartProps {
      override val width: UndefOr[Int] = aWidth.asUndefOr
      override val height: UndefOr[Int] = aHeight.asUndefOr
      override val margin: UndefOr[Margin] = aMargin.asUndefOr
    }
  }
}
