package jp.seraphr.narou.commands.download

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.Future
import scala.io.{ Source, StdIn }
import scala.util.{ Failure, Try }
import scala.util.control.NonFatal

import jp.seraphr.command.Command
import jp.seraphr.narou.{ HasLogger, NovelDownloader }
import jp.seraphr.narou.NovelDownloader.DownloadResult
import jp.seraphr.narou.commands.download.DownloadNovelCommand.DownloadNovelCommandArg
import jp.seraphr.narou.model.NarouNovel

import io.circe.generic.auto._
import io.circe.parser._
import monix.execution.Scheduler

/** 小説ダウンロードコマンド */
class DownloadNovelCommand(aDefaultArg: DownloadNovelCommandArg) extends Command with HasLogger {
  override val name                 = "download"
  override val description          = "collectコマンドにより収集した小説一覧を元に、小説をダウンロードし、ファイルに保存します"
  override val version              = "0.1.0"
  private val mParser               = new OptionParser(aDefaultArg)
  implicit val scheduler: Scheduler = Scheduler.global

  override def run(aArgs: Seq[String]): Try[Unit] = {
    mParser.parse(aArgs) match {
      case Some(tArgs) => download(tArgs)
      case None        => Failure(new RuntimeException(s"fail to parse command line args: ${aArgs.mkString("[", ",", "]")}"))
    }
  }

  implicit class LoanPattern[A <: { def close(): Unit }](a: A) extends HasLogger {
    import scala.reflect.Selectable.reflectiveSelectable
    def loan[B](f: A => B): B = {
      try f(a)
      finally
        try a.close()
        catch {
          case NonFatal(e) => logger.warn("[skip] リソースのクローズに失敗しました。 この例外を無視します。 ", e)
        }
    }

  }

  private def download(aArgs: DownloadNovelCommandArg): Try[Unit] = Try {
    val tDownloader  = new NovelDownloader(aArgs.output, aArgs.intervalMillis)
    val tStopBoolean = new AtomicBoolean(false)

    logger.info("ダウンロードを中断する場合は、エンターキーを押してください。")
    readStdInput().foreach { _ =>
      logger.info("現在のノベルのダウンロード完了時に、ダウンロード全体を停止します。")
      tStopBoolean.set(true)
    }

    val tResult =
      Source
        .fromFile(aArgs.input, "UTF-8")
        .loan { tLines =>
          val tNovels = tLines.getLines().map(line => decode[NarouNovel](line)).collect { case Right(novel) => novel }

          val downloadTask = tDownloader.downloadNovels(tNovels, aArgs.overwrite)
          val results      = downloadTask.runSyncUnsafe()

          var tLastResult = DownloadResult(0, 0)
          results
            .takeWhile(_.novelCount < aArgs.maxNovels && !tStopBoolean.get())
            .foreach { r =>
              if (tLastResult != r) {
                tLastResult = r
                logger.info(s"${r.novelCount} / ${aArgs.maxNovels} (${r.pageCount} pages)")
              }
            }

          tLastResult
        }

    logger.info(s"${tResult.novelCount} 小説 合計${tResult.pageCount}話のダウンロードを行いました")
  }

  private def readStdInput(): Future[String] = Future {
    scala
      .concurrent
      .blocking {
        StdIn.readLine()
      }
  }

  class OptionParser(aDefaultArg: DownloadNovelCommandArg) extends CommandArgParser[DownloadNovelCommandArg] {
    def parse(aArgs: Seq[String]): Option[DownloadNovelCommandArg] = this.parse(aArgs, aDefaultArg)

    note("ダウンロード中に、エンターキーを押下すると、ダウンロードが中断されます。")

    opt[File]('o', "output")
      .optional()
      .text(s"出力先ファイルパスを指定します。 省略した場合は、${aDefaultArg.output}です。")
      .action((f, c) => c.copy(output = f))

    opt[File]('i', "input")
      .optional()
      .text(s"入力に使用する、ノベル一覧ファイルを指定します。 省略した場合は、${aDefaultArg.input}です。")
      .action((f, c) => c.copy(input = f))

    opt[Long]('l', "interval")
      .optional()
      .text(s"なろう小説APIへのアクセスインターバルをミリ秒単位で指定します。 省略した場合は、${aDefaultArg.intervalMillis}です")
      .action((i, c) => c.copy(intervalMillis = i))

    opt[Unit]("overwrite")
      .optional()
      .text(s"このオプションを指定した場合、すでにダウンロード済みのファイルを再ダウンロードします。")
      .action((_, c) => c.copy(overwrite = true))

    opt[Int]('n', "num")
      .optional()
      .text(s"ダウンロードする最大ノベル数を指定します。 省略した場合は、${aDefaultArg.maxNovels}です")
      .action((n, c) => c.copy(maxNovels = n))
  }
}

object DownloadNovelCommand {
  case class DownloadNovelCommandArg(
      input: File,
      output: File,
      overwrite: Boolean,
      intervalMillis: Long,
      maxNovels: Int
  )
}
