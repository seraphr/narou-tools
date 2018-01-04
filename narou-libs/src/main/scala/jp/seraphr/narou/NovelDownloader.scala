package jp.seraphr.narou

import java.io.File
import java.nio.charset.StandardCharsets

import jp.seraphr.narou.NovelDownloader.DownloadResult
import narou4j.Narou
import narou4j.entities.Novel
import org.apache.commons.io.FileUtils

/**
 */
class NovelDownloader(aTargetDir: File, aIntervalMillis: Long) extends HasLogger {
  private val mInvalidCharSet = """\/:*?"<>|.""".toSet

  /**
   *
   * @param aNovel
   * @param aOverride
   * @return
   */
  def downloadNovel(aNovel: Novel, aOverride: Boolean = false): DownloadResult = {
    val tNovelDir = new File(aTargetDir, s"${aNovel.getNcode}_${aNovel.getTitle.filterNot(c => c.isControl || mInvalidCharSet(c)).take(10).trim}")
    tNovelDir.mkdirs()
    val tClient = new MyNarou

    // アクセス頻度の調整
    val tAdjuster = new IntervalAdjuster(aIntervalMillis)
    var tResult = false

    val tMaxPage = tNovelDir.list().map(_.toInt).reduceOption(_ max _).getOrElse(0)
    val tAllNovels = (1 to aNovel.getAllNumberOfNovel).filter(tMaxPage < _ || aOverride)

    tAllNovels.foreach { i =>
      val tNovelFile = new File(tNovelDir, i.toString)
      logger.info(s"${aNovel.getNcode} ${i} / ${aNovel.getAllNumberOfNovel}")
      tAdjuster.adjust()
      FileUtils.deleteQuietly(tNovelFile)
      val tBody = tClient.getNovelBody(aNovel.getNcode, i)
      FileUtils.write(tNovelFile, tBody.getBody, StandardCharsets.UTF_8)
    }

    val tPageCount = tAllNovels.size
    val tNovelCount = if (0 < tPageCount) 1 else 0
    DownloadResult(tNovelCount, tPageCount)
  }

  def downloadNovels(aNovels: Iterator[Novel], aOverride: Boolean = false): Iterator[DownloadResult] = {
    def filterExists(aNovels: Seq[Novel]): Iterator[Novel] = {
      import scala.collection.JavaConverters._

      val tFiltered =
        NarouClientBuilder.init
          .n(_.setNCode(aNovels.map(_.getNcode).toArray))
          .skipLim(0, 500)
          .buildFromEmpty
          .getNovels.asScala

      // 先頭は、allCountのみが含まれている奴なので、削る
      tFiltered.tail.iterator
    }

    aNovels
      .filter(_.getNovelType == 1) // 短編がgetNovelBodyに失敗するので、とりあえず取らない
      .grouped(40).flatMap(filterExists) // 40小説ずつ、存在するものだけを残す
      .map(n => () => downloadNovel(n, aOverride))
      .scanLeft(() => DownloadResult(0, 0)) {
        (tAccThunk, tResult) =>
          // * scanLeftを噛ませると、最小限必要な要素よりも1個多く値の評価を行ってしまうのに対応するため、関数に包む
          // * Accの評価も遅延させなければならない
          // * 関数内で評価してしまうと
          // ** 要素数が多い時にstackoverflowする
          // ** downloadが何度も評価されてしまう
          // * ので lazy val にする
          lazy val tAcc = tAccThunk()
          () => tAcc merge tResult()
      }.map(_())
  }
}

object NovelDownloader {
  case class DownloadResult(novelCount: Int, pageCount: Int) {
    def merge(aThat: DownloadResult): DownloadResult = DownloadResult(this.novelCount + aThat.novelCount, this.pageCount + aThat.pageCount)
  }
}