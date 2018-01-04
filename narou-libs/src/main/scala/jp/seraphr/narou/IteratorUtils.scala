package jp.seraphr.narou

/**
 */
object IteratorUtils {
  class IteratorPlusOne[A](aIterator: Iterator[A], f: A => Boolean) extends Iterator[A] {
    private var mFinished = false

    override def hasNext: Boolean = !mFinished && aIterator.hasNext
    override def next(): A = {
      if (mFinished) throw new NoSuchElementException("next on empty iterator")
      aIterator.next().tap(n => mFinished = !f(n))
    }
  }

  implicit class IteratorOpts[A](val aIterator: Iterator[A]) extends AnyVal {
    /**
     * 元のイテレータから、最初に条件を満たさない要素が見つかるまでtakeします。
     * takeWhileに加えて追加でひとつの要素を取得するメソッドです。
     *
     * @param f
     * @return
     */
    def takeWhileOne(f: A => Boolean): Iterator[A] = new IteratorPlusOne[A](aIterator, f)
  }
}
