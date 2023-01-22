package jp.seraphr.recharts

import org.scalajs.dom.Element
import scala.scalajs.js
import scala.scalajs.js.|

import japgolly.scalajs.react.ReactMouseEventFrom
import typings.recharts.{ components, scatterMod }
import typings.recharts.rechartsStrings.{ category, left, number, right }
import typings.recharts.scatterMod.ScatterPointItem
import typings.recharts.utilTypesMod.AdaptChildMouseEventHandler

object Implicits {
  implicit class ScatterOps(val s: components.Scatter.type) extends AnyVal {
    def create(aName: js.UndefOr[String], aPoints: js.UndefOr[Seq[ScatterPointItem]]) = {
      components.Scatter(
        aName.asInstanceOf[js.UndefOr[String] with (js.UndefOr[String | Double])],
        aPoints.asInstanceOf[js.UndefOr[String] with js.UndefOr[js.Array[ScatterPointItem]]]
      )
    }

  }

  implicit class ScatterProps(val s: scatterMod.Props.type) extends AnyVal {
    def create(aName: js.UndefOr[String], aPoints: js.UndefOr[Seq[ScatterPointItem]]) = {
      s(
        aName.asInstanceOf[js.UndefOr[String] with (js.UndefOr[String | Double])],
        aPoints.asInstanceOf[js.UndefOr[String] with js.UndefOr[js.Array[ScatterPointItem]]]
      )
    }

  }

  implicit class YAxisOps(val y: components.YAxis.type) extends AnyVal {
    def create() = {
      components.YAxis(
        js.undefined.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]],
        js.undefined.asInstanceOf[(js.UndefOr[Double | String]) with (js.UndefOr[left | right])],
        js.undefined.asInstanceOf[js.UndefOr[String] with (js.UndefOr[number | category])],
        js.undefined.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]]
      )
    }

  }

  implicit class LegendOps(val l: components.Legend.type) extends AnyVal {
    def create() = {
      components.Legend(
        js.undefined.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]],
        js.undefined
          .asInstanceOf[
            (js.UndefOr[js.Function1[ /* event */ ReactMouseEventFrom[Element], Unit]])
              with (js.UndefOr[
                AdaptChildMouseEventHandler[Any, japgolly.scalajs.react.facade.React.Element]
              ])
          ],
        js.undefined
          .asInstanceOf[
            (js.UndefOr[js.Function1[ /* event */ ReactMouseEventFrom[Element], Unit]])
              with (js.UndefOr[
                AdaptChildMouseEventHandler[Any, japgolly.scalajs.react.facade.React.Element]
              ])
          ],
        js.undefined
          .asInstanceOf[
            (js.UndefOr[js.Function1[ /* event */ ReactMouseEventFrom[Element], Unit]])
              with (js.UndefOr[
                AdaptChildMouseEventHandler[Any, japgolly.scalajs.react.facade.React.Element]
              ])
          ],
        js.undefined.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]]
      )
    }

  }

  implicit class LabelOps(val l: components.Label.type) extends AnyVal {
    def create(offset: js.UndefOr[String] = js.undefined) = {
      components.Label(
        offset.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]]
      )
    }

  }

  implicit class ReferenceDotOps(val d: components.ReferenceDot.type) extends AnyVal {
    def create(
        className: js.UndefOr[String],
        cx: js.UndefOr[String | Double],
        cy: js.UndefOr[String | Double],
        r: js.UndefOr[String | Double]
    ) = {
      components.ReferenceDot(
        className.asInstanceOf[js.UndefOr[String] with (js.UndefOr[Double | String])],
        cx.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]],
        cy.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]],
        r.asInstanceOf[(js.UndefOr[Double | String]) with js.UndefOr[Double]]
      )
    }

  }

}
