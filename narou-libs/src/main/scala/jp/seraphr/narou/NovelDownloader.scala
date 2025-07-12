package jp.seraphr.narou

import java.io.File
import java.nio.charset.StandardCharsets

import jp.seraphr.narou.NovelDownloader.DownloadResult
import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.{ NovelInfo, SearchParams }
import jp.seraphr.narou.model.NarouNovel

import monix.eval.Task
import monix.execution.Scheduler
import org.apache.commons.io.FileUtils

/** 小説ダウンローダー */
class NovelDownloader(aTargetDir: File, aIntervalMillis: Long) extends HasLogger {
  private val mInvalidCharSet       = """\/:*?"<>|.""".toSet
  implicit val scheduler: Scheduler = Scheduler.global
  private val client                = NarouApiClient().runSyncUnsafe()
  private val myNarou               = new MyNarou

  /**
   * 小説をダウンロードする
   * @param aNarouNovel 小説情報
   * @param aOverride 既存ファイルを上書きするか
   * @return ダウンロード結果
   */
  def downloadNovel(aNarouNovel: NarouNovel, aOverride: Boolean = false): Task[DownloadResult] = {
    val tNovelDir = new File(
      aTargetDir,
      s"${aNarouNovel.ncode}_${aNarouNovel.title.filterNot(c => c.isControl || mInvalidCharSet(c)).take(10).trim}"
    )
    tNovelDir.mkdirs()

    // アクセス頻度の調整
    val tAdjuster = new IntervalAdjuster(aIntervalMillis)

    val tMaxPage   = Option(tNovelDir.list()).getOrElse(Array.empty[String]).flatMap(_.toIntOption).maxOption.getOrElse(0)
    val tAllNovels = (1 to aNarouNovel.chapterCount).filter(tMaxPage < _ || aOverride)

    val downloadTasks = tAllNovels.map { i =>
      val tNovelFile = new File(tNovelDir, i.toString)
      logger.info(s"${aNarouNovel.ncode} ${i} / ${aNarouNovel.chapterCount}")
      tAdjuster.adjust()
      FileUtils.deleteQuietly(tNovelFile)

      myNarou
        .getNovelBody(aNarouNovel.ncode, i)
        .map { tBody =>
          FileUtils.write(tNovelFile, tBody.body, StandardCharsets.UTF_8)
        }
    }

    Task
      .sequence(downloadTasks)
      .map { _ =>
        val tPageCount  = tAllNovels.size
        val tNovelCount = if (0 < tPageCount) 1 else 0
        DownloadResult(tNovelCount, tPageCount)
      }
  }

  class LazyDownload(aNarouNovel: NarouNovel, aOverride: Boolean) {
    lazy val mResult        = downloadNovel(aNarouNovel, aOverride).runSyncUnsafe()
    def get: DownloadResult = mResult
  }

  def downloadNovels(aNarouNovels: Iterator[NarouNovel], aOverride: Boolean = false): Task[Iterator[DownloadResult]] = {
    def filterExists(aNarouNovels: Seq[NarouNovel]): Task[Iterator[NovelInfo]] = {
      val params = SearchParams(ncode = aNarouNovels.map(_.ncode), lim = Some(500))
      client.search(params).map(_.novels.iterator)
    }

    val novelSeq = aNarouNovels
      .filter(_.novelType == jp.seraphr.narou.model.NovelType.Serially) // 短編は除外
      .toSeq

    val groupedTasks = novelSeq
      .grouped(40)
      .map { group =>
        filterExists(group).flatMap { existingNovels =>
          val existingNcodes = existingNovels.map(_.ncode).toSet
          val filteredGroup  = group.filter(n => existingNcodes.contains(n.ncode))

          Task.sequence(filteredGroup.map { novel =>
            downloadNovel(novel, aOverride)
          })
        }
      }

    Task.sequence(groupedTasks.toList).map(_.flatten.iterator)
  }

}

object NovelDownloader {
  case class DownloadResult(novelCount: Int, pageCount: Int) {
    def merge(aThat: DownloadResult): DownloadResult =
      DownloadResult(this.novelCount + aThat.novelCount, this.pageCount + aThat.pageCount)

  }
}
