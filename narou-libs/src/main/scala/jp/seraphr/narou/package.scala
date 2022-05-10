package jp.seraphr

/**
 */
package object narou {
  implicit class Tapper[A](val a: A) extends AnyVal {
    def tap[U](f: A => U): A = { f(a); a }
  }

  implicit class OrderingOps(val ord: Ordering.type) extends AnyVal {
    def compare[A](l: A, r: A)(implicit ordering: Ordering[A]): Int = {
      ordering.compare(l, r)
    }

  }
}
