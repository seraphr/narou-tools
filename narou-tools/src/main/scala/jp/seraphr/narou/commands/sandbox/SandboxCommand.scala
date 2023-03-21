package jp.seraphr.narou.commands.sandbox

import java.io.File

import scala.annotation.nowarn
import scala.io.Source
import scala.util.{ Failure, Try, Using }

import jp.seraphr.command.Command
import jp.seraphr.narou.{ DefaultNarouNovelsWriter, FileNovelDataAccessor, HasLogger }

import com.fasterxml.jackson.databind.ObjectMapper
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import narou4j.entities.Novel

class SandboxCommand(aDefaultArg: SandboxCommandArg) extends Command with HasLogger {
  private val mParser                             = new OptionParser(aDefaultArg)
  override val name                               = "sandbox"
  override val description                        = "適当に色々やるための入り口"
  override val version                            = "0.1.0"
  override def run(aArgs: Seq[String]): Try[Unit] = {
    mParser.parse(aArgs) match {
      case Some(tArgs) => runSandbox(tArgs)
      case None        => Failure(new RuntimeException(s"fail to parse command line args: ${aArgs.mkString("[", ",", "]")}"))
    }
  }

  implicit class FileOps(file: File) {
    def /(aChild: String)            = new File(file, aChild)
    def modName(f: String => String) = new File(file.getParentFile, f(file.getName))
  }

  private def runSandbox(aArgs: SandboxCommandArg): Try[Unit] = Try {
    import com.fasterxml.jackson.core.`type`.TypeReference
    val tMapper: ObjectMapper = new ObjectMapper

    logger.info(s"小説情報の読み込み ${aArgs.input}")
    val tNovels = Using(Source.fromFile(aArgs.input, "UTF-8")) { tLines =>
      tLines.getLines().map(tMapper.readValue[Novel](_, new TypeReference[Novel]() {})).toVector
    }.get

    //    showKeywords(tNovels)
    convertNovelList(tNovels, aArgs.input.modName(_ => "novels"))
    //    findUploadType0(tNovels)
  }

  // unusedは scala 3.3で復活する
  //  @nowarn("cat=unused")
  private def findUploadType0(aNovels: Vector[Novel]): Unit = {
    val tFiltered = aNovels.filter(_.getUploadType == 0)
    logger.info(s"count = ${tFiltered.size}")
    logger.info(s"全短編数 = ${aNovels.count(_.getNovelType == 2)}")
    logger.info(s"短編 = ${tFiltered.count(_.getNovelType == 2)}")
    logger.info(s"長編 = ${tFiltered.count(_.getNovelType == 1)}")
    tFiltered
      .filter(_.getNovelType == 1)
      .take(10)
      .foreach(n => logger.info(s"ncode=${n.getNcode}, length=${n.getNumberOfChar}"))
  }

  // unusedは scala 3.3で復活する
  //  @nowarn("cat=unused")
  private def showKeywords(aNovels: Vector[Novel]): Unit = {
    logger.info(s"keyword情報取得")
    val tSortedKeywords = aNovels
      .flatMap(_.getKeyword.split(" "))
      .groupBy(identity)
      .view
      .mapValues(_.size)
      .toSeq
      .sortBy(-_._2)
    val tKeywordCount   = tSortedKeywords.size
    logger.info(s"総キーワード数: ${tKeywordCount}")

    logger.info(s"上位10件")
    tSortedKeywords.take((10)).foreach(println)

    logger.info(s"中間10件")
    tSortedKeywords.drop(tKeywordCount / 2 - 5).take(10).foreach(println)

    logger.info(s"下位10件")
    tSortedKeywords.takeRight((10)).foreach(println)
  }

  //  @nowarn("cat=unused")
  private def convertNovelList(aNovels: Vector[Novel], aOutputDir: File): Unit = {
    logger.info(s"novelの変換を行います: Novel数=${aNovels.size}")
    import jp.seraphr.narou.model.NarouNovelConverter._

    val tNovels = Observable
      .fromIterable(aNovels)
      .zipWithIndex
      .map { case (tNovel, tIndex) =>
        val tCount = tIndex + 1
        if (tCount % 5000 == 0) {
          logger.info(s"変換: ${tCount}")
        }
        tNovel.asScala
      }

    val tDataWriter = new FileNovelDataAccessor(aOutputDir.getParentFile)
    new DefaultNarouNovelsWriter("all", tDataWriter, aOutputDir.getName, 50000).write(tNovels).runSyncUnsafe()
    logger.info(s"novelの変換が完了しました")
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
