package jp.seraphr.narou

import narou4j.Narou
import narou4j.entities.Novel
import narou4j.enums.OutputOrder

import scala.jdk.CollectionConverters
import scala.collection.mutable.ListBuffer

case class NarouRankSettings(buildNarou: Narou => Narou, filter: Novel => Boolean, limit: Int) {
  def n(m: Narou => Unit): NarouRankSettings = {
    val g: Narou => Narou = _.tap(m)
    this.copy(buildNarou = buildNarou andThen g)
  }

  def addFilter(f: Novel => Boolean): NarouRankSettings = {
    val tNewFilter: Novel => Boolean = n => f(n) && filter(n)

    this.copy(filter = tNewFilter)
  }

  def limit(aLimit: Int) = this.copy(limit = aLimit)
}
object NarouRankSettings {
  def apply(): NarouRankSettings = NarouRankSettings(identity, _ => true, 100)
}

case class NarouRankResult(novels: Seq[NovelAndRate], allNovelCount: Int, uniqueNovelCount: Int, filteredCount: Int)
case class NovelAndRate(novel: Novel, rate: Double)

/**
 */
class NarouRankGenerator() {
  def generateRank(aSettings: NarouRankSettings): NarouRankResult = {
    import CollectionConverters._
    val tSkips = List(0, 500, 1000, 1500, 2000)
    val tOrders = OutputOrder.values().toList
    val tNovelBuffer = new ListBuffer[Novel]

    for {
      tOrder <- tOrders
      tSkip <- tSkips
      tSettings = if (tSkip == 0) aSettings else aSettings.n(_.setSt(tSkip))
      tNarou = tSettings.n(_.setLim(500)).n(_.setOrder(tOrder)).buildNarou(new Narou)
      tNovels = tNarou.getNovels
      _ = println(s"${(tOrder, tSkip)}")
    } tNovelBuffer ++= tNovels.asScala

    val tNovels = tNovelBuffer.result()

    val tAllCount = tNovels.size

    val tUniqueNovels = tNovels.groupBy(_.getNcode).map(_._2.head).toVector
    val tUniqueCount = tUniqueNovels.size

    val tFiltered = tUniqueNovels.filter(aSettings.filter)
    val tFilteredCount = tFiltered.size

    val tSorted = tFiltered.map(n => NovelAndRate(n, (n.getAllPoint.toDouble / n.getFavCount))).sortBy(-_.rate).take(aSettings.limit)

    NarouRankResult(tSorted, tAllCount, tUniqueCount, tFilteredCount)
  }
}
