package jp.seraphr.narou

import java.io.File
import java.nio.charset.StandardCharsets

import scala.io.Source

import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.{ NovelInfo, SearchParams }
import jp.seraphr.narou.model.{ NarouNovel, NovelType }
import jp.seraphr.narou.model.NarouNovelConverter._

import io.circe.generic.auto._
import io.circe.parser._
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable
import org.apache.commons.io.FileUtils

/** 旧式の小説ダウンローダー（互換性維持用） */
class OldNovelDownloader(aRootDir: File) {
  private val mInvalidCharSet       = """\/:*?"<>|.""".toSet
  private val myNarou               = new MyNarou
  implicit val scheduler: Scheduler = Scheduler.global

  /**
   * 小説をダウンロードする
   * @param aNarouNovel 小説情報
   * @param aOverride 既存ファイルを上書きするか
   * @return 一つ以上のファイルをダウンロードしたらtrue
   */
  def downloadNovel(aNarouNovel: NarouNovel, aOverride: Boolean = false): Boolean = {
    val tNovelDir  = new File(
      aRootDir,
      s"${aNarouNovel.ncode}_${aNarouNovel.title.filterNot(c => c.isControl || mInvalidCharSet(c)).take(10).trim}"
    )
    tNovelDir.mkdirs()
    val tAllNovels = 1 to aNarouNovel.chapterCount

    // 1秒に1回しかアクセスさせない
    val tAdjuster = new IntervalAdjuster(2000)
    var tResult   = false

    val tMaxPage = Option(tNovelDir.list()).getOrElse(Array.empty[String]).flatMap(_.toIntOption).maxOption.getOrElse(0)

    tAllNovels
      .filter(tMaxPage < _ || aOverride)
      .foreach { i =>
        val tNovelFile = new File(tNovelDir, i.toString)
        println(s"${aNarouNovel.ncode} ${i} / ${aNarouNovel.chapterCount}")
        tAdjuster.adjust()
        tResult = true
        FileUtils.deleteQuietly(tNovelFile)
        val tBody      = myNarou.getNovelBody(aNarouNovel.ncode, i).runSyncUnsafe()
        FileUtils.write(tNovelFile, tBody.body, StandardCharsets.UTF_8)
      }

    if (tResult) println()
    tResult
  }

}

object OldNovelDownloaderMain extends App {
  import monix.execution.Scheduler.Implicits.global

  val client  = NarouApiClient().runSyncUnsafe()
  val tNovels = Source
    .fromFile(new File("./novellist"), "UTF-8")
    .getLines()
    .map(line => decode[NarouNovel](line))
    .collect { case Right(novel) => novel }

  def filterExists(aNovels: Seq[NarouNovel]): Task[Iterator[NovelInfo]] = {
    val params = SearchParams(ncode = aNovels.map(_.ncode), lim = Some(500))
    client.search(params).map(_.novels.iterator)
  }

  val task = Observable
    .fromIterator(Task.pure(tNovels))
    .filter(_.novelType == NovelType.Serially) // 短編がgetNovelBodyに失敗するので、とりあえず取らない
    .bufferTumbling(40)
    .mapEval { group =>
      filterExists(group).map { existingNovels =>
        val existingNcodes = existingNovels.map(_.ncode).toSet
        group.filter(n => existingNcodes.contains(n.ncode))
      }
    }
    .flatMap(Observable.fromIterable)
    .map { n =>
      new OldNovelDownloader(new File("./novels")).downloadNovel(n)
    }
    .filter(identity)                          // ダウンロードが1つ以上行われたものを数える
    .take(500)                                 // とりあえず500ノベルダウンロード
    .zipWithIndex
    .foreachL(println)

  task.runSyncUnsafe()
}
