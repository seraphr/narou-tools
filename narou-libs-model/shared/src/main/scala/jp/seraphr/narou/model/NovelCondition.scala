package jp.seraphr.narou.model

case class NovelCondition(id: String, name: String, predicate: NarouNovel => Boolean) {
  def and(aThat: NovelCondition): NovelCondition = {
    NovelCondition(s"${this.id}_and_${aThat.id}", s"${this.name} & ${aThat.name}", n => this.predicate(n) && aThat.predicate(n))
  }

  def andQuietly(aThat: NovelCondition): NovelCondition = {
    this.copy(predicate = n => this.predicate(n) && aThat.predicate(n))
  }

  def not: NovelCondition = {
    NovelCondition(s"not_${this.id}", s"not ${this.name}", n => !this.predicate(n))
  }
}
object NovelCondition {
  val all = NovelCondition("all", "all", _ => true)
  val finished = NovelCondition("finished", "完結済み", _.isFinished)
  val length100k = NovelCondition("length100k", "10万字以上", 100000 <= _.length)
  val length1m = NovelCondition("length100k", "100万字以上", 1000000 <= _.length)
  val bookmark100 = NovelCondition("bookmark100", "100 <= bookmark", 100 <= _.bookmarkCount)

  def bookmark(aMin: Int, aMax: Int) =
    NovelCondition(s"bookmark${aMin}-${aMax}", s"${aMin} <= bookmark < ${aMin}", n => aMin <= n.bookmarkCount && n.bookmarkCount < aMax)

  val bookmark0_500 = bookmark(0, 500)
  val bookmark500_1000 = bookmark(500, 1000)
  val bookmark1k_3k = bookmark(1000, 3000)
  val bookmark3k = bookmark(3000, 1000000)
}
