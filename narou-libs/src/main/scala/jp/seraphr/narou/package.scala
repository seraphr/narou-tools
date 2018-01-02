package jp.seraphr

/**
 */
package object narou {
  implicit class Tapper[A](val a: A) extends AnyVal {
    def tap[U](f: A => U): A = { f(a); a }
  }
}
