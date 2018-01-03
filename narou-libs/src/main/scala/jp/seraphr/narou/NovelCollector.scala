package jp.seraphr.narou

import narou4j.Narou
import narou4j.entities.Novel
import narou4j.enums.{ NovelGenre, OutputOrder }

import scala.collection.JavaConverters

/**
 */
class NovelCollector(aIntervalMillis: Long) extends HasLogger {
  private val mMaxSkip = 2000
  private val mLimit = 500

  def collect(aBuilder: NarouClientBuilder): Iterator[Novel] = {
    import JavaConverters._
    def tSkips = (0 to mMaxSkip by mLimit).iterator
    def tOrders = (None +: OutputOrder.values().toList.map(Option(_))).iterator
    def tGenres = NovelGenre.values().iterator
    def tKeywords = (None +: NovelCollector.keywords.map(Option(_))).iterator
    def tPickUps = Iterator(None, Some(true), Some(false))
    case class FilterSetting(genre: NovelGenre, isPickup: Option[Boolean], keyword: Option[Narou => Unit])
    case class OrderSettings(order: Option[OutputOrder])
    case class Setting(filter: FilterSetting, order: OrderSettings, skip: Int)
    // アクセス頻度を調整
    val tAdjuster = new IntervalAdjuster(aIntervalMillis)

    def collectOne(aSetting: Setting): Vector[Novel] = {
      logger.info(aSetting.toString)
      tAdjuster.adjust()

      val tGenre = aSetting.filter.genre
      val tPickup = aSetting.filter.isPickup
      val tKeyword = aSetting.filter.keyword
      val tOrder = aSetting.order.order
      val tSkip = aSetting.skip
      aBuilder
        .genre(tGenre)
        .opt(_.n)(tKeyword)
        .opt(_.pickup)(tPickup)
        .opt(_.order)(tOrder)
        .skipLim(tSkip, mLimit)
        .buildFromEmpty
        .getNovels.asScala.tail.toVector // 先頭はallcountだけが入っているデータなので削る
    }

    def collectBySettings(aRemainSettings: List[Setting]): Iterator[Novel] = aRemainSettings match {
      case Nil => Iterator.empty
      case h :: t =>
        val tHead = collectOne(h)
        if (tHead.size < mLimit) {
          def otherFilter(s: Setting) = h.filter != s.filter
          def sameFilterWithSmallSkip(s: Setting) = h.filter == s.filter && h.order == s.order && h.skip >= s.skip
          val tNewRemain = t.filter(s => otherFilter(s) || sameFilterWithSmallSkip(s))
          tHead.iterator ++ collectBySettings(tNewRemain)
        } else {
          tHead.iterator ++ collectBySettings(t)
        }
    }

    val tSettings =
      for {
        tGenre <- tGenres
        tPickup <- tPickUps
        tKeyword <- tKeywords
        tOrder <- tOrders
        tSkip <- tSkips
      } yield Setting(FilterSetting(tGenre, tPickup, tKeyword), OrderSettings(tOrder), tSkip)

    collectBySettings(tSettings.toList)
  }
}

object NovelCollector {
  private def keyword[U](f: Narou => Boolean => Unit): Narou => Unit = { tNarou =>
    f(tNarou)(true)
  }

  val keywords = Seq(
    keyword(_.setR15),
    keyword(_.setBl),
    keyword(_.setGl),
    keyword(_.setZankoku),
    keyword(_.setTensei),
    keyword(_.setTenni)
  )
}
