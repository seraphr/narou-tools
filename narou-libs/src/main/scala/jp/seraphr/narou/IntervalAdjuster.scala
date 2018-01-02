package jp.seraphr.narou

/**
 */
class IntervalAdjuster(aIntervalMillis: Long) {
  private var mLast = 0L

  def adjust(): Unit = {
    val tSleep = aIntervalMillis - (System.currentTimeMillis() - mLast)
    if (0 < tSleep) Thread.sleep(tSleep)
    mLast = System.currentTimeMillis()
  }
}
