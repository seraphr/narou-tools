package jp.seraphr.narou.commands.collect

import java.io.File
import java.time.LocalDate

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Try }
import scala.util.control.NonFatal

import jp.seraphr.command.Command
import jp.seraphr.narou.{
  AllNovelCollector,
  DefaultExtractedNovelLoader,
  DropboxNovelDataAccessor,
  ExtractedNarouNovelsWriter,
  FileNovelDataAccessor,
  HasLogger,
  NarouClientBuilder,
  NovelDataReader
}
import jp.seraphr.narou.commands.collect.CollectNovelCommand._
import jp.seraphr.narou.model.{ NarouNovel, NovelCondition }

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import monix.execution.Scheduler
import monix.reactive.Observable
import scopt.Read

/**
 */
class CollectNovelCommand(aDefaultArg: CollectNovelCommandArg)(implicit scheduler: Scheduler)
    extends Command
    with HasLogger {
  private val mParser                             = new OptionParser(aDefaultArg)
  override val name                               = "collect"
  override val description                        = "なろう小説の一覧を収集し、ファイルに保存します"
  override val version                            = "0.1.0"
  override def run(aArgs: Seq[String]): Try[Unit] = {
    mParser.parse(aArgs) match {
      case Some(tArgs) => collect(tArgs)
      case None        => Failure(new RuntimeException(s"fail to parse command line args: ${aArgs.mkString("[", ",", "]")}"))
    }
  }

  implicit class LoanPattern[A <: { def close(): Unit }](a: A) extends HasLogger {
    import scala.language.reflectiveCalls
    def loan[B](f: A => B): B = {
      try f(a)
      finally
        try a.close()
        catch {
          case NonFatal(e) => logger.warn("[skip] リソースのクローズに失敗しました。 この例外を無視します。 ", e)
        }
    }

  }

  private def collect(aArg: CollectNovelCommandArg): Try[Unit] = Try {
    def loadFrom(aReader: NovelDataReader): Map[String, NarouNovel] = {
      val tNovelsObs = new DefaultExtractedNovelLoader(aReader).loadAll

      val tFuture = tNovelsObs.foldLeftL(Map.empty[String, NarouNovel])((map, n) => map.updated(n.ncode, n)).runToFuture
      Await.result(tFuture, Duration.Inf)
    }

    lazy val tDropboxClient = {
      val tConfig = DbxRequestConfig
        .newBuilder("narou-tool-collector/0.1")
        .withUserLocale("ja-JP")
        .withAutoRetryEnabled()
        .build()
      new DbxClientV2(tConfig, DropboxApp.newCredential())
    }

    val tOutput             = aArg.output
    val tOutputDataAccessor = tOutput match {
      case Local(file)   =>
        logger.info(s"ローカルファイルシステムへの出力を行います: ${file}")
        new FileNovelDataAccessor(file)
      case Dropbox(path) =>
        val tPath = path.getOrElse(LocalDate.now().toString)
        logger.info(s"Dropboxへの出力を行います: ${tPath}")
        new DropboxNovelDataAccessor(tDropboxClient, tPath)
    }

    val tInitMap = (tOutputDataAccessor.exists().runSyncUnsafe(), aArg.overwrite) match {
      case (false, _)       => Map.empty[String, NarouNovel]
      case (true, Fail)     => throw new RuntimeException(s"出力先がすでに存在します: ${tOutput}")
      case (true, Recreate) =>
        logger.info("既存の出力ファイルを消して再生成します")
        Map.empty[String, NarouNovel]
      case (true, Update)   =>
        logger.info("既存の出力ファイルに情報を追加します")
        loadFrom(tOutputDataAccessor)
    }

    //    val tCollector = new NovelCollector(aArg.intervalMillis)
    val tCollector = new AllNovelCollector(aArg.intervalMillis)
    val tInitSize  = tInitMap.size
    logger.info(s"小説リストの収集を開始します。 初期ノベル数: ${tInitSize}")

    val tResultMap  = tCollector
      .collect(NarouClientBuilder.init)
      .take(aArg.limit)
      .foldLeft(tInitMap) {
        import jp.seraphr.narou.model.NarouNovelConverter._
        (m, n) => m.updated(n.getNcode, n.asScala)
      }
    val tResultSize = tResultMap.size
    logger.info(s"収集が完了しました。 最終ノベル数: ${tResultSize}  増加ノベル数: ${tResultSize - tInitSize}")

    val tConditions = Seq(
      NovelCondition.all,
      NovelCondition.length100k,
      NovelCondition.length300k and NovelCondition.bookmark1000,
      NovelCondition.length500k and NovelCondition.bookmark1000,
      NovelCondition.length100k and NovelCondition.bookmark1000
    )
    val tNovels     = Observable.fromIterable(tResultMap.values)

    val tTask = for {
      tBackup      <- tOutputDataAccessor.backup(".bak")
      _             = tBackup.foreach { tBackupPath =>
                        logger.info(s"既存データをバックアップしました: ${tBackupPath}")
                      }
      tNovelsWriter = new ExtractedNarouNovelsWriter(tOutputDataAccessor, tConditions, aArg.novelsPerFile)
      _             = logger.info(s"出力を開始します。")
      _            <- tNovelsWriter.write(tNovels)
      _             = logger.info(s"出力が完了しました。")
    } yield ()

    tTask.runSyncUnsafe()
  }

//  private def outputToLocal(aNovels: Observable[NarouNovel], aOutputDir: File, createNovelsWriter: NovelDataWriter => NarouNovelsWriter): Unit = {
//    val tTempOutputDir = Files.createTempDirectory("novel_list").toFile
//    try {
//      logger.info(s"一時ファイルへの書き込みを開始します")
//      val tWriter = new FileNovelDataAccessor(tTempOutputDir)
//      createNovelsWriter(tWriter).write(aNovels).runSyncUnsafe()
//      logger.info(s"一時ファイルへの書き込みを完了しました。")
//      logger.info(s"出力ファイルの差し替えを行います。")
//      if (aOutputDir.exists()) {
//        val tBackupDir = new File(aOutputDir.getParentFile, s"${aOutputDir.getName}.bak")
//        if (tBackupDir.exists()) {
//          FileUtils.deleteQuietly(tBackupDir)
//        }
//        FileUtils.moveDirectory(aOutputDir, tBackupDir)
//        logger.info(s"旧出力ファイルを、バックアップしました: ${tBackupDir.getCanonicalPath}")
//      }
//      FileUtils.moveDirectory(tTempOutputDir, aOutputDir)
//      logger.info(s"出力が完了しました。")
//    } finally {
//      FileUtils.deleteQuietly(tTempOutputDir)
//    }
//  }

  class OptionParser(aDefaultArg: CollectNovelCommandArg) extends CommandArgParser[CollectNovelCommandArg] {
    def parse(aArgs: Seq[String]): Option[CollectNovelCommandArg] = this.parse(aArgs, aDefaultArg)

    implicit private val mReadOutput: Read[OutputOption] = {
      val tLocalPrefix   = "local:"
      val tDropboxPrefix = "dropbox:"
      Read.reads { tInput =>
        if (tInput.startsWith(tLocalPrefix)) {
          val tPath = tInput.substring(tLocalPrefix.length)
          if (tPath.isEmpty) throw new RuntimeException("pathは指定できません")
          Local(new File(tPath))
        } else if (tInput.startsWith(tDropboxPrefix)) {
          val tPath = tInput.substring(tDropboxPrefix.length)
          if (tPath.isEmpty) throw new RuntimeException("pathは指定できません")
          Dropbox(Some(tPath))
        } else if (tInput == "dropbox") {
          Dropbox(None)
        } else {
          throw new RuntimeException(s"invalid input(=${tInput}) for out")
        }
      }
    }

    opt[OutputOption]('o', "out")
      .optional()
      .valueName(s"<local:$${path} | dropbox[:$${path}]>")
      .text(s"""出力先ファイルパスを指定します。 省略した場合は、${aDefaultArg.output}です。
           |dropboxのパスを省略した場合、実行日の日付のディレクトリを使用します。
           |""".stripMargin)
      .action((f, c) => c.copy(output = f))

    opt[Long]('i', "interval")
      .optional()
      .text(s"なろう小説APIへのアクセスインターバルをミリ秒単位で指定します。 省略した場合は、${aDefaultArg.intervalMillis}です")
      .action((i, c) => c.copy(intervalMillis = i))

    opt[Int]('l', "limit")
      .optional()
      .text(s"取得する小説の最大数を指定します。 省略した場合は、${aDefaultArg.limit}です")
      .action((i, c) => c.copy(limit = i))

    opt[Int]("novelsPerFile")
      .optional()
      .text(s"1ファイルにいくつの小説を格納するかを指定します。。 省略した場合は、${aDefaultArg.novelsPerFile}です")
      .action((i, c) => c.copy(novelsPerFile = i))

    implicit private val mReadOverwrite: Read[OverwriteOption] = Read.reads {
      case Recreate.text => Recreate
      case Update.text   => Update
      case Fail.text     => Fail
      case etc           => throw new Exception(s"invalid input(=${etc}) for overwrite")
    }

    opt[OverwriteOption]("overwrite")
      .optional()
      .valueName("recreate | update | fail")
      .text(s"出力先ファイルが既にある場合の動作を指定します。 省略した場合は、${aDefaultArg.overwrite.text}です")
      .action((o, c) => c.copy(overwrite = o))
  }
}

object CollectNovelCommand {
  sealed trait OverwriteOption {
    val text: String
  }

  /** 削除して作り直す */
  case object Recreate extends OverwriteOption {
    override val text: String = "recreate"
  }

  /** 既存ファイルの情報を読み込んでアップデート */
  case object Update extends OverwriteOption {
    override val text: String = "update"
  }

  /** 失敗させる */
  case object Fail extends OverwriteOption {
    override val text: String = "fail"
  }

  sealed trait OutputOption
  case class Local(file: File)             extends OutputOption {
    override def toString = s"local:${file}"
  }
  case class Dropbox(path: Option[String]) extends OutputOption {
    override def toString: String = {
      val tPath = path.fold("")(":" + _)
      s"dropbox${tPath}"
    }

  }

  case class CollectNovelCommandArg(
      output: OutputOption,
      overwrite: OverwriteOption,
      intervalMillis: Long,
      limit: Int,
      novelsPerFile: Int
  )
}
