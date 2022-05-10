package jp.seraphr.js

import scala.scalajs.js

object ScalaJsConverters {
  implicit class OptionOps[A](o: Option[A]) {
    def asUndefOr: js.UndefOr[A] = o.fold[js.UndefOr[A]](js.undefined)(a => a)
  }

  implicit class AnyOops[A](a: A) {
    def asDefined: js.UndefOr[A] = a
    def cast[B]: B               = a.asInstanceOf[B]
    def asAny: js.Any            = a.asInstanceOf[js.Any]
  }
}
