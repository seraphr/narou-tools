package jp.seraphr

import scala.scalajs.js
import japgolly.scalajs.react._

package object recharts {
  type ContentRenderer[P] = js.Function1[P, vdom.VdomNode]
}
