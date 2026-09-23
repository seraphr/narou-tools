package jp.seraphr.recharts

import scala.scalajs.js

import typings.recharts.components

object Implicits {
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

}
