package jp.seraphr.recharts

import scala.scalajs.js
import scala.scalajs.js.|

import typings.recharts.{ components, typesCartesianScatterMod => scatterMod }
import typings.recharts.typesCartesianScatterMod.ScatterPointItem

object Implicits {
  implicit class ScatterOps(val s: components.Scatter.type) extends AnyVal {
    def create(aName: String, aPoints: Seq[ScatterPointItem]) = {
      components
        .Scatter
        .withProps(
          scatterMod.Props.create(aName, aPoints)
        )
    }

  }

  implicit class ScatterProps(val s: scatterMod.Props.type) extends AnyVal {
    def create(aName: String, aPoints: Seq[ScatterPointItem]) = {
      import js.JSConverters._
      scatterMod.ScatterProps().setName(aName).setPoints(aPoints.toJSArray).asInstanceOf[scatterMod.Props]
    }

  }

  implicit class YAxisOps(val y: components.YAxis.type) extends AnyVal {
    def create() = {
      y()
    }

  }

  implicit class LegendOps(val l: components.Legend.type) extends AnyVal {
    def create() = {
      l()
    }

  }

  implicit class LabelOps(val l: components.Label.type) extends AnyVal {
    def create(offset: js.UndefOr[String] = js.undefined) = {
      offset.fold(l())(l().offset(_))
    }

  }

  implicit class RefOpts[B](private val b: B)                         extends AnyVal {
    def withOps[A](v: js.UndefOr[A])(set: B => A => B): B = v.fold(b)(set(b))
  }
  implicit class ReferenceDotOps(val d: components.ReferenceDot.type) extends AnyVal {
    def create(
        className: js.UndefOr[String],
        cx: js.UndefOr[Double | String],
        cy: js.UndefOr[Double | String],
        r: js.UndefOr[Double | String]
    ) = {
      d().withOps(className)(_.className).withOps(cx)(_.cx).withOps(cy)(_.cy).withOps(r)(_.r)
    }

  }

}
