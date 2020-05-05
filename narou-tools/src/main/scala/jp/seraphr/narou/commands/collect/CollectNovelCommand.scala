package jp.seraphr.narou.commands.collect

import java.io.{ File, FileOutputStream, OutputStreamWriter }
import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ ObjectMapper, SerializerProvider }
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import jp.seraphr.command.Command
import jp.seraphr.narou.{ AllNovelCollector, HasLogger, NarouClientBuilder }
import jp.seraphr.narou.commands.collect.CollectNovelCommand._
import narou4j.entities.Novel
import narou4j.enums.NovelGenre
import org.apache.commons.io.FileUtils
import scopt.Read

import scala.io.Source
import scala.util.control.NonFatal
import scala.util.{ Failure, Try }

/**
 */
class CollectNovelCommand(aDefaultArg: CollectNovelCommandArg) extends Command with HasLogger {
  private val mParser = new OptionParser(aDefaultArg)
  override val name = "collect"
  override val description = "なろう小説の一覧を収集し、ファイルに保存します"
  override val version = "0.1.0"
  override def run(aArgs: Seq[String]): Try[Unit] = {
    mParser.parse(aArgs) match {
      case Some(tArgs) => collect(tArgs)
      case None        => Failure(new RuntimeException(s"fail to parse command line args: ${aArgs.mkString("[", ",", "]")}"))
    }
  }

  object GenreSerializer extends StdSerializer[NovelGenre](classOf[NovelGenre]) {
    override def serialize(value: NovelGenre, gen: JsonGenerator, provider: SerializerProvider): Unit = {
      gen.writeNumber(value.getId)
    }
  }

  private val mMapper = {
    import jp.seraphr.narou.Tapper
    new ObjectMapper().tap { tMapper =>
      tMapper.registerModule(new SimpleModule().tap(_.addSerializer(classOf[NovelGenre], GenreSerializer)))
    }
  }

  implicit class LoanPattern[A <: { def close(): Unit }](a: A) extends HasLogger {
    import scala.language.reflectiveCalls
    def loan[B](f: A => B): B = {
      try f(a) finally try a.close() catch {
        case NonFatal(e) => logger.warn("[skip] リソースのクローズに失敗しました。 この例外を無視します。 ", e)
      }
    }
  }

  private def collect(aArg: CollectNovelCommandArg): Try[Unit] = Try {
    def loadFrom(aFile: File): Map[String, Novel] = {
      Source.fromFile(aFile, "UTF-8").loan {
        _.getLines
          .map(mMapper.readValue[Novel](_, new TypeReference[Novel]() {}))
          .foldLeft(Map.empty[String, Novel])((m, n) => m.updated(n.getNcode, n))
      }
    }

    val tOutput = aArg.output
    if (!tOutput.getParentFile.exists()) {
      tOutput.getParentFile.mkdirs()
    }

    val tInitMap = (tOutput.exists(), aArg.overwrite) match {
      case (false, _)   => Map.empty[String, Novel]
      case (true, Fail) => throw new RuntimeException(s"出力先がすでに存在します: ${tOutput.getCanonicalPath}")
      case (true, Recreate) =>
        logger.info("既存の出力ファイルを消して再生成します")
        Map.empty[String, Novel]
      case (true, Update) =>
        logger.info("既存の出力ファイルに情報を追加します")
        loadFrom(tOutput)
    }

    //    val tCollector = new NovelCollector(aArg.intervalMillis)
    val tCollector = new AllNovelCollector(aArg.intervalMillis)
    val tTempOutputFile = File.createTempFile("novel_list", "")
    val tInitSize = tInitMap.size
    logger.info(s"小説リストの収集を開始します。 初期ノベル数: ${tInitSize}")
    try {
      val tResultMap = tCollector.collect(NarouClientBuilder.init).foldLeft(tInitMap) {
        (m, n) => m.updated(n.getNcode, n)
      }
      val tResultSize = tResultMap.size
      logger.info(s"収集が完了しました。 最終ノベル数: ${tResultSize}  増加ノベル数: ${tResultSize - tInitSize}")
      logger.info(s"一時ファイルへの書き込みを開始します")
      new OutputStreamWriter(new FileOutputStream(tTempOutputFile), StandardCharsets.UTF_8).loan { tWriter =>
        tResultMap.values.foreach { tNovel =>
          tWriter.write(mMapper.writeValueAsString(tNovel))
          tWriter.write("\n")
        }
      }
      logger.info(s"一時ファイルへの書き込みを完了しました。")
      logger.info(s"出力ファイルの差し替えを行います。")
      if (tOutput.exists()) {
        val tBackupFile = new File(tOutput.getParentFile, s"${tOutput.getName}.bak")
        if (tBackupFile.exists()) {
          FileUtils.deleteQuietly(tBackupFile)
          logger.info(s"旧出力ファイルを、バックアップしました: ${tBackupFile.getCanonicalPath}")
        }
        FileUtils.moveFile(tOutput, tBackupFile)
      }
      FileUtils.moveFile(tTempOutputFile, tOutput)
      logger.info(s"出力が完了しました。")
    } finally {
      FileUtils.deleteQuietly(tTempOutputFile)
    }
  }

  class OptionParser(aDefaultArg: CollectNovelCommandArg) extends CommandArgParser[CollectNovelCommandArg] {
    def parse(aArgs: Seq[String]): Option[CollectNovelCommandArg] = this.parse(aArgs, aDefaultArg)

    opt[File]('o', "out")
      .optional()
      .text(s"出力先ファイルパスを指定します。 省略した場合は、${aDefaultArg.output}です")
      .action((f, c) => c.copy(output = f))

    opt[Long]('i', "interval")
      .optional()
      .text(s"なろう小説APIへのアクセスインターバルをミリ秒単位で指定します。 省略した場合は、${aDefaultArg.intervalMillis}です")
      .action((i, c) => c.copy(intervalMillis = i))

    private implicit val mReadOverwhite: Read[OverwriteOption] = Read.reads {
      case Recreate.text => Recreate
      case Update.text   => Update
      case Fail.text     => Fail
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

  case class CollectNovelCommandArg(
    output: File,
    overwrite: OverwriteOption,
    intervalMillis: Long
  )
}