package jp.seraphr

import scala.scalajs.js
import japgolly.scalajs.react._

import scala.scalajs.js.|

package object recharts {
  type ContentRenderer[P] = js.Function1[P, vdom.VdomNode]
  type DataKey[T] = String | Int | js.Function1[T, js.Any]

  type ReactElement[E] = vdom.VdomElement

  type LegendType = String
  object LegendType {
    val plainline = "plainline"
    val line = "line"
    val square = "square"
    val rect = "rect"
    val circle = "circle"
    val cross = "cross"
    val diamond = "diamond"
    val star = "star"
    val triangle = "triangle"
    val wye = "wye"
    val none = "none"
  }

  type TooltipType = String
  object TooltipType {
    val none = "none"
  }

  type SymbolType = String
  object SymbolType {
    val circle = "circle"
    val cross = "cross"
    val diamond = "diamond"
    val square = "square"
    val star = "star"
    val triangle = "triangle"
    val wye = "wye"
  }

  type AnimationTiming = String
  object AnimationTiming {
    val ease = "ease"
    val `ease-in` = "ease-in"
    val `ease-out` = "ease-out"
    val `ease-in-out` = "ease-in-out"
    val linear = "linear"
  }
}
