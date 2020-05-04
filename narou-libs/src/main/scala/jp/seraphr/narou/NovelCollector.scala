package jp.seraphr.narou

import narou4j.Narou
import narou4j.entities.Novel
import narou4j.enums.{ NovelGenre, OutputOrder }

class KeywordFilter private (val build: Narou => Unit, val bits: Int, name: String) {
  override def toString: String = s"keyword=${name}"
}
object KeywordFilter {
  private var bits = 1
  private def keyword[U](f: Narou => Boolean => Unit, aValue: Boolean, aName: String): KeywordFilter = {
    val tBits = bits
    bits <<= 1
    new KeywordFilter(tNarou => f(tNarou)(true), tBits, aName)
  }

  // positive negative keyword
  private def pnKeyword(f: Narou => Boolean => Unit, aName: String): Set[KeywordFilter] = {
    Set(
      keyword(f, true, s"+${aName}"),
      keyword(f, false, s"-${aName}")
    )
  }

  val all = Set(
    pnKeyword(_.setR15, "R15"),
    pnKeyword(_.setBl, "BL"),
    pnKeyword(_.setGl, "GL"),
    pnKeyword(_.setZankoku, "残酷"),
    pnKeyword(_.setTensei, "転生"),
    pnKeyword(_.setTenni, "転移")
  ).flatten
}

case class SearchFilter(genre: NovelGenre, isPickup: Option[Boolean], keywords: Set[KeywordFilter]) {
  /** thisの検索結果がthatの検索結果を包含している場合true */
  def include(that: SearchFilter): Boolean = {
    if (this.genre != that.genre) return false
    if (this.isPickup != that.isPickup) return false
    val lks = this.keywords
    val rks = that.keywords

    // ジャンルとpickupが同じで、必須キーワードが包含関係にある場合検索結果も包含関係となる
    lks subsetOf rks
  }
}
object SearchFilter {
  val all = for {
    genre <- NovelGenre.values().toVector
    isPickup <- Vector(None, Some(true), Some(false))
    keyword <- KeywordFilter.all.subsets()
  } yield SearchFilter(genre, isPickup, keyword)

  implicit object KeywordsSetOrdering extends Ordering[Set[KeywordFilter]] {
    override def compare(lks: Set[KeywordFilter], rks: Set[KeywordFilter]): Int = {
      // bitsの総和を比較に利用する
      // bitsの特性上  lks ⊆ rks => tLeftBits <= tRightBits が成り立つ
      // そのため、これをそのまま比較に用いることで、包含関係を考慮にいれた全順序が作れる
      // 一般的な包含関係による順序と逆にしているのは、検索結果の包含関係で大小をつけたいため
      val tLeftBits = lks.foldLeft(0)(_ + _.bits)
      val tRightBits = rks.foldLeft(0)(_ + _.bits)
      Ordering.compare(tRightBits, tLeftBits)
    }
  }

  private def orderingBy[A: Ordering](f: SearchFilter => A) = Ordering.by(f)
  implicit val filterOrdering: Ordering[SearchFilter] = {
    orderingBy(_.genre.getId()) orElse orderingBy(_.isPickup) orElse orderingBy(_.keywords)
  }
}

case class SearchOrder(order: OutputOrder)
object SearchOrder {
  val all = OutputOrder.values().toList.map(SearchOrder(_))

  implicit object SearchOrderOrdering extends Ordering[SearchOrder] {
    override def compare(x: SearchOrder, y: SearchOrder): Int = Ordering.compare(x.order.getId(), y.order.getId())
  }
}

case class SearchSetting(filter: SearchFilter, order: SearchOrder, skip: Int)

object SearchSetting {
  val maxSkip = 2000
  val limit = 500
  private val skipAll = (0 to maxSkip by limit)

  def allIterator = for {
    filter <- SearchFilter.all.iterator
    order <- SearchOrder.all
    skip <- skipAll
  } yield SearchSetting(filter, order, skip)

  lazy val all = allIterator.toList

  private def orderingBy[A: Ordering](f: SearchSetting => A) = Ordering.by(f)

  // ソートしたとき、検索結果が多いものを先頭側にしたいので、reverseしておく
  implicit val SearchSettingOrdering: Ordering[SearchSetting] = {
    orderingBy(_.filter) orElse orderingBy(_.order) orElse orderingBy(_.skip)
  }.reverse
}

/**
 */
class NovelCollector(aIntervalMillis: Long) extends HasLogger {
  private val mLimit = SearchSetting.limit

  def collect(aBuilder: NarouClientBuilder, aFilter: SearchSetting => Boolean = _ => true): Iterator[Novel] = {
    // アクセス頻度を調整
    val tAdjuster = new IntervalAdjuster(aIntervalMillis)
    val tInitSettings = SearchSetting.all.filter(aFilter).toVector.sorted
    val tInitSettingsSize = tInitSettings.size

    def collectOne(aSetting: SearchSetting): Vector[Novel] = {
      tAdjuster.adjust()

      val tGenre = aSetting.filter.genre
      val tPickup = aSetting.filter.isPickup
      val tKeywords = aSetting.filter.keywords
      val tOrder = aSetting.order.order
      val tSkip = aSetting.skip

      import scala.jdk.CollectionConverters._
      aBuilder
        .genre(tGenre)
        .seq(_.n)(tKeywords.toSeq.map(_.build))
        .opt(_.pickup)(tPickup)
        .order(tOrder)
        .skipLim(tSkip, mLimit)
        .buildFromEmpty
        .getNovels.asScala.tail.toVector // 先頭はallcountだけが入っているデータなので削る
    }

    def collectBySettings(aRemainSettings: Vector[SearchSetting]): Iterator[Novel] = aRemainSettings match {
      case Vector() => Iterator.empty
      case Vector(h, t @ _*) =>
        val tRemainSize = t.size
        if (tRemainSize % 100 == 0) {
          logger.info(h.toString)
          logger.info(f"初期検索条件=${tInitSettingsSize} 残り検索条件=${tRemainSize} 進捗=${100.0 - (tRemainSize.toDouble / tInitSettingsSize * 100)}%.4f%%")
        }
        val tHead = collectOne(h)
        if (tHead.size < mLimit) {
          // Limitに達し無かったとき、同じ検索条件のものはすべて発見したことになるので、以下の検索条件を削る
          // * 検索結果が包含関係にある（= 検索結果がより小さい）もの
          //     * ただし、検索条件とorderが同じものは削らない
          //         * 同条件でskipが小さいものがありうるので
          // 「同じ検索条件且つ同じorderで、skipが大きいもの」も削っていいけど、誤差なので削らない
          def includedFilter(that: SearchSetting): Boolean = {
            h.filter.include(that.filter)
          }

          def sameFilterAndOrder(that: SearchSetting): Boolean = {
            h.filter == that.filter && h.order == that.order
          }

          val (tRemoved, tNewRemain) = t.partition(that => includedFilter(that) && !sameFilterAndOrder(that))
          logger.info(s"検索件数(skip=${h.skip}, size=${tHead.size})が最大値未満だったため、${tRemoved.size} 件の検索条件を削除しました。 remain=${tNewRemain.size}。 条件: ${h.filter}")
          tHead.iterator ++ collectBySettings(tNewRemain.toVector)
        } else {
          tHead.iterator ++ collectBySettings(t.toVector)
        }
    }

    logger.info(s"全 ${tInitSettingsSize} 個の検索条件で検索を行います。")
    collectBySettings(tInitSettings)
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
