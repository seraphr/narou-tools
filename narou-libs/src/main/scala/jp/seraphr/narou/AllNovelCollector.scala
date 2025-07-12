package jp.seraphr.narou

import java.text.NumberFormat
import java.util.concurrent.atomic.AtomicInteger

import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.model.NarouNovel

import monix.eval.Task
import monix.execution.Scheduler

class AllNovelCollector(aIntervalMillis: Long)(implicit scheduler: Scheduler) extends HasLogger {
  private val mLimit   = 500
  private val mMaxSkip = 2000

  def collect(aBuilder: NarouClientBuilder, client: NarouApiClient): Iterator[NarouNovel] = {
    val tAdjuster = new IntervalAdjuster(aIntervalMillis)
    val tCounter  = new AtomicInteger(0)
    val tFormat   = NumberFormat.getNumberInstance()

    def collectOne(aMinLength: Int, aSkip: Int, aRemainRetry: Int = 4): Vector[NarouNovel] = {
      tAdjuster.adjust()
      val tCount = tCounter.incrementAndGet()
      if (tCount % 100 == 0) {
        val tNovelCount = tFormat.format(mLimit * tCount) // 重複があるので、正確なカウントじゃないけど・・・
        logger.info(s"${tCount} 回目の呼び出しです。 minLength=${aMinLength}, skip=${aSkip} limit*count=${tNovelCount}")
      }

      import jp.seraphr.narou.api.model.OrderType
      val tOrder = OrderType.LengthAsc // 文字数昇順

      try {
        val result = aBuilder
          .order(tOrder)
          .length(Some(aMinLength), None)
          .skipLim(aSkip, mLimit)
          .search(client)
          .runSyncUnsafe()

        import ApiNovelConverter._
        result.novels.map(_.asScala).toVector
      } catch {
        case e: Throwable if 0 < aRemainRetry =>
          logger.warn(s"[retry] 例外が発生したため、リトライを行います。 remain = ${aRemainRetry}: ${e.getMessage}")
          val tSleep = math.pow(2, 5 - aRemainRetry).toInt
          Thread.sleep(tSleep * 1000)

          // 雑だけど、リトライ前にdecrementしておく
          tCounter.decrementAndGet()
          collectOne(aMinLength, aSkip, aRemainRetry - 1)
      }
    }

    def collectAll(aMinLength: Int = 0, aSkip: Int = 0): Iterator[NarouNovel] = {
      val tCollected  = collectOne(aMinLength, aSkip)
      val tHeadLength = tCollected.head.length
      val tLastLength = tCollected.last.length

      logger.debug(
        s"found ${tCollected.size}, minLength=${aMinLength}, aSkip=${aSkip}, head=${tHeadLength}, last=${tLastLength}"
      )

      if (tCollected.size < mLimit) {
        // 終端に達したので探索終了
        logger.info(s"探索の終端に到達しました。 minLength=${aMinLength}, lastLength${tLastLength}")
        return tCollected.iterator
      }

      if (aMinLength == tLastLength && mMaxSkip <= aSkip) {
        // まさかの200文字の小説が4500個以上あるというアレ
        // ジャンルとかで分ければ、行ける気がするけど、とりあえずいいや・・・
        logger.warn(s"[Skip] length=${tHeadLength}の小説が2500件以上見つかりました。 これ以上を無視します。")
        tCollected.iterator ++ collectAll(tLastLength + 1, 0)
      } else if (aSkip < mMaxSkip) {
        // skipが最大に達してなければ、skipを増やして次
        tCollected.iterator ++ collectAll(aMinLength, aSkip + mLimit)
      } else {
        // skipが最大に達していたら、minLengthを最後の小説のlengthにしてskip=0からスタート
        tCollected.iterator ++ collectAll(tLastLength, 0)
      }
    }

    collectAll()
  }

}
