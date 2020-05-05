package jp.seraphr.narou.commands.sandbox

import java.io.File
import java.nio.file.{ Files, StandardOpenOption }

import com.fasterxml.jackson.databind.ObjectMapper
import jp.seraphr.command.Command
import jp.seraphr.narou.HasLogger
import narou4j.entities.Novel

import scala.io.Source
import scala.util.{ Failure, Try, Using }

class SandboxCommand(aDefaultArg: SandboxCommandArg) extends Command with HasLogger {
  private val mParser = new OptionParser(aDefaultArg)
  override val name = "sandbox"
  override val description = "適当に色々やるための入り口"
  override val version = "0.1.0"
  override def run(aArgs: Seq[String]): Try[Unit] = {
    mParser.parse(aArgs) match {
      case Some(tArgs) => runSandbox(tArgs)
      case None        => Failure(new RuntimeException(s"fail to parse command line args: ${aArgs.mkString("[", ",", "]")}"))
    }
  }

  private def runSandbox(aArgs: SandboxCommandArg): Try[Unit] = Try {
    import com.fasterxml.jackson.core.`type`.TypeReference
    val tMapper: ObjectMapper = new ObjectMapper

    logger.info(s"小説情報の読み込み ${aArgs.input}")
    val tNovels = Using(Source.fromFile(aArgs.input, "UTF-8")) { tLines =>
      tLines.getLines.map(tMapper.readValue[Novel](_, new TypeReference[Novel]() {})).toVector
    }.get

    showKeywords(tNovels)
    convertNovelList(tNovels, new File(aArgs.input.getParentFile, aArgs.input.getName + ".jsonl"))
  }

  private def showKeywords(aNovels: Vector[Novel]): Unit = {
    logger.info(s"keyword情報取得")
    val tSortedKeywords = aNovels.flatMap(_.getKeyword.split(" ")).groupBy(identity).view.mapValues(_.size).toSeq.sortBy(-_._2)
    val tKeywordCount = tSortedKeywords.size
    logger.info(s"総キーワード数: ${tKeywordCount}")

    logger.info(s"上位10件")
    tSortedKeywords.take((10)).foreach(println)

    logger.info(s"中間10件")
    tSortedKeywords.drop(tKeywordCount / 2 - 5).take(10).foreach(println)

    logger.info(s"下位10件")
    tSortedKeywords.takeRight((10)).foreach(println)
  }

  private def convertNovelList(aNovels: Vector[Novel], aOutput: File): Unit = {
    import io.circe.syntax._
    import jp.seraphr.narou.json.NarouNovelFormats._
    import jp.seraphr.narou.model.NarouNovelConverter._

    def newOutputStream = Files.newBufferedWriter(aOutput.toPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)

    Using(newOutputStream) { tStream =>
      aNovels.foreach { n =>
        tStream.write(n.asScala.asJson.noSpaces)
        tStream.write("\n")
      }
    }
  }

  class OptionParser(aDefaultArg: SandboxCommandArg) extends CommandArgParser[SandboxCommandArg] {
    def parse(aArgs: Seq[String]): Option[SandboxCommandArg] = this.parse(aArgs, aDefaultArg)

    opt[File]('i', "input")
      .optional()
      .text(s"入力に使用する、ノベル一覧ファイルを指定します。 省略した場合は、${aDefaultArg.input}です。")
      .action((f, c) => c.copy(input = f))
  }
}

case class SandboxCommandArg(input: File)