package jp.seraphr.narou.commands.narou

import java.io.File

import scala.util.{ Failure, Try }

import jp.seraphr.command.{ Command, ParentCommand }
import jp.seraphr.narou.HasLogger
import jp.seraphr.narou.commands.collect.CollectNovelCommand
import jp.seraphr.narou.commands.collect.CollectNovelCommand.CollectNovelCommandArg
import jp.seraphr.narou.commands.download.DownloadNovelCommand
import jp.seraphr.narou.commands.download.DownloadNovelCommand.DownloadNovelCommandArg
import jp.seraphr.narou.commands.sandbox.{ SandboxCommand, SandboxCommandArg }

/**
 */
class NarouCommand(aSubCommands: Seq[Command]) extends ParentCommand with HasLogger {
  override protected val subCommands: Seq[Command] = aSubCommands
  override val name                                = "narou"
  override val description                         = "なろう小説を処理する各種ツールの親コマンドです"
  override val version                             = "0.1.0"
  override def run(aArgs: Seq[String]): Try[Unit]  = {
    val tStartTime = System.currentTimeMillis()
    val tResult    = super.run(aArgs)
    val tTimeSpan  = System.currentTimeMillis() - tStartTime
    logger.info(s"narou command finished: ${tTimeSpan} ms (= ${tTimeSpan / (60 * 1000)} minutes)")
    tResult
  }

}

object NarouCommand extends HasLogger {
  import monix.execution.Scheduler.Implicits.global

  private val mSubCommands = Seq(
    new CollectNovelCommand(
      CollectNovelCommandArg(
        CollectNovelCommand.Dropbox(None),
        CollectNovelCommand.Update,
        1000,
        limit = Int.MaxValue,
        novelsPerFile = 10000,
        withAll = false
      )
    ),
    new DownloadNovelCommand(DownloadNovelCommandArg(new File("./novel_list"), new File("./novels"), false, 1000, 100)),
    new SandboxCommand(SandboxCommandArg(new File("./narou_novels")))
  )

  def main(aArgs: Array[String]): Unit = {
    new NarouCommand(mSubCommands).run(aArgs.toSeq) match {
      case Failure(e) => logger.error("コマンドの実行に失敗しました", e)
      case _          =>
    }
  }

}
