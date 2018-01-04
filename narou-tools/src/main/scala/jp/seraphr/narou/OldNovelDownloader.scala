package jp.seraphr.narou

import java.io.File
import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.databind.ObjectMapper
import narou4j.Narou
import narou4j.entities.Novel
import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters
import scala.io.Source

/**
 */
class OldNovelDownloader(aRootDir: File) {
  private val mInvalidCharSet = """\/:*?"<>|.""".toSet

  /**
   *
   * @param aNovel
   * @param aOverride
   * @return 一つ以上のファイルをダウンロードしたらtrue
   */
  def downloadNovel(aNovel: Novel, aOverride: Boolean = false): Boolean = {
    val tNovelDir = new File(aRootDir, s"${aNovel.getNcode}_${aNovel.getTitle.filterNot(c => c.isControl || mInvalidCharSet(c)).take(10).trim}")
    tNovelDir.mkdirs()
    val tClient = new MyNarou
    val tAllNovels = 1 to aNovel.getAllNumberOfNovel

    // 1秒に1回しかアクセスさせない
    val tAdjuster = new IntervalAdjuster(2000)
    var tResult = false

    val tMaxPage = tNovelDir.list().map(_.toInt).reduceOption(_ max _).getOrElse(0)

    tAllNovels.filter(tMaxPage < _ || aOverride).foreach { i =>
      val tNovelFile = new File(tNovelDir, i.toString)
      println(s"${aNovel.getNcode} ${i} / ${aNovel.getAllNumberOfNovel}")
      tAdjuster.adjust()
      tResult = true
      FileUtils.deleteQuietly(tNovelFile)
      val tBody = tClient.getNovelBody(aNovel.getNcode, i)
      FileUtils.write(tNovelFile, tBody.getBody, StandardCharsets.UTF_8)
    }

    if (tResult) println()
    tResult
  }
}

object OldNovelDownloaderMain extends App {

  import com.fasterxml.jackson.core.`type`.TypeReference

  val tMapper: ObjectMapper = new ObjectMapper
  val tNovels = Source.fromFile(new File("./novellist"), "UTF-8").getLines.map(tMapper.readValue[Novel](_, new TypeReference[Novel]() {}))

  def filterExists(aNovels: Seq[Novel]): Iterator[Novel] = {
    import JavaConverters._

    val tFiltered =
      NarouClientBuilder.init
        .n(_.setNCode(aNovels.map(_.getNcode).toArray))
        .skipLim(0, 500)
        .build(new Narou)
        .getNovels.asScala

    // 先頭は、allCountのみが含まれている奴なので、削る
    tFiltered.tail.iterator
  }

  tNovels
    .filter(_.getNovelType == 1) // 短編がgetNovelBodyに失敗するので、とりあえず取らない
    .grouped(40).flatMap(filterExists) // 40小説ずつ、存在するものだけを残す
    .map { n =>
      new OldNovelDownloader(new File("./novels")).downloadNovel(n)
    }
    .filter(identity) // ダウンロードが1つ以上行われたものを数える
    .take(500) // とりあえず1000ノベルダウンロード
    .zipWithIndex.foreach(println)

}