package jp.seraphr.js

import scala.scalajs.js

object ScalaJsConverters {
  implicit class OptionOps[A](o: Option[A]) {
    def asUndefOr: js.UndefOr[A] = o.fold[js.UndefOr[A]](js.undefined)(a => a)
  }
}
