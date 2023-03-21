package jp.seraphr.narou.model

case class NovelCondition(id: String, name: String, predicate: NarouNovel => Boolean) {
  def and(aThat: NovelCondition): NovelCondition = {
    NovelCondition(
      s"${this.id}_and_${aThat.id}",
      s"${this.name} & ${aThat.name}",
      n => this.predicate(n) && aThat.predicate(n)
    )
  }

  def andQuietly(aThat: NovelCondition): NovelCondition = {
    this.copy(predicate = n => this.predicate(n) && aThat.predicate(n))
  }

  def not: NovelCondition = {
    if (this == NovelCondition.finished) NovelCondition.notFinished
    else if (this == NovelCondition.notFinished) NovelCondition.finished
    else NovelCondition(s"not_${this.id}", s"not ${this.name}", n => !this.predicate(n))
  }

}
object NovelCondition {
  val all: NovelCondition          = NovelCondition("all", "all", _ => true)
  val finished: NovelCondition     = NovelCondition("finished", "完結済み", _.isFinished)
  val notFinished: NovelCondition  = NovelCondition("notFinished", "連載中", !_.isFinished)
  val length100k: NovelCondition   = NovelCondition("length100k", "10万字以上", 100000 <= _.length)
  val length300k: NovelCondition   = NovelCondition("length300k", "30万字以上", 300000 <= _.length)
  val length500k: NovelCondition   = NovelCondition("length500k", "50万字以上", 500000 <= _.length)
  val length1m: NovelCondition     = NovelCondition("length100k", "100万字以上", 1000000 <= _.length)
  val bookmark100: NovelCondition  = NovelCondition("bookmark100", "100 <= bookmark", 100 <= _.bookmarkCount)
  val bookmark1000: NovelCondition = NovelCondition("bookmark1000", "1000 <= bookmark", 1000 <= _.bookmarkCount)

  def bookmark(aMin: Int, aMax: Int): NovelCondition =
    NovelCondition(
      s"bookmark${aMin}-${aMax}",
      s"${aMin} <= bookmark < ${aMax}",
      n => aMin <= n.bookmarkCount && n.bookmarkCount < aMax
    )

  val bookmark0_500: NovelCondition    = bookmark(0, 500)
  val bookmark500_1000: NovelCondition = bookmark(500, 1000)
  val bookmark1k_3k: NovelCondition    = bookmark(1000, 3000)
  val bookmark3k: NovelCondition       = bookmark(3000, 1000000)
}
