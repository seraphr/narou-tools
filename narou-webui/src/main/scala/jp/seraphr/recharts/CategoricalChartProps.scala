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
    aTop: js.UndefOr[Int] = js.undefined,
    aRight: js.UndefOr[Int] = js.undefined,
    aBottom: js.UndefOr[Int] = js.undefined,
    aLeft: js.UndefOr[Int] = js.undefined
  ): Margin = {
    new Margin {
      override val top: UndefOr[Int] = aTop
      override val right: UndefOr[Int] = aRight
      override val bottom: UndefOr[Int] = aBottom
      override val left: UndefOr[Int] = aLeft
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
    aWidth: js.UndefOr[Int] = js.undefined,
    aHeight: js.UndefOr[Int] = js.undefined,
    aMargin: js.UndefOr[Margin] = js.undefined
  ): CategoricalChartProps = {
    new CategoricalChartProps {
      override val width: UndefOr[Int] = aWidth
      override val height: UndefOr[Int] = aHeight
      override val margin: UndefOr[Margin] = aMargin
    }
  }
}
