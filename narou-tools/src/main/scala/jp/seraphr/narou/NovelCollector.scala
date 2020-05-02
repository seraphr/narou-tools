package jp.seraphr.narou

import java.io.{ File, FileOutputStream, OutputStreamWriter }
import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.{ ObjectMapper, SerializerProvider }
import narou4j.Narou
import narou4j.entities.Novel
import narou4j.enums.{ NovelGenre, OutputOrder }

import scala.jdk.CollectionConverters

/**
 */
object OldNovelCollector {
  def collect(aBuilder: NarouClientBuilder): Iterator[Novel] = {
    import CollectionConverters._
    def tSkips = Iterator(0, 500, 1000, 1500, 2000)
    def tOrders = (None +: OutputOrder.values().toList.map(Option(_))).iterator
    def tGenres = NovelGenre.values().iterator
    case class Setting(genre: NovelGenre, order: Option[OutputOrder], skip: Int)
    // 1秒に1回しかアクセスさせない
    val tAdjuster = new IntervalAdjuster(1000)

    def collectOne(aSetting: Setting): Vector[Novel] = {
      println(aSetting)
      tAdjuster.adjust()

      val tGenre = aSetting.genre
      val tOrder = aSetting.order
      val tSkip = aSetting.skip
      aBuilder.genre(tGenre).opt(_.order)(tOrder).skipLim(tSkip, 500).build(new Narou).getNovels.asScala.tail.toVector // 先頭はallcountだけが入っているデータなので削る
    }

    def collectBySettings(aRemainSettings: List[Setting]): Iterator[Novel] = aRemainSettings match {
      case Nil => Iterator.empty
      case h :: t =>
        val tHead = collectOne(h)
        if (tHead.size < 500) {
          def otherGenre(s: Setting) = h.genre != s.genre
          def sameOrderGenre(s: Setting) = h.genre == s.genre && h.order == s.order && h.skip >= s.skip
          val tNewRemain = t.filter(s => otherGenre(s) || sameOrderGenre(s))
          tHead.iterator ++ collectBySettings(tNewRemain)
        } else {
          tHead.iterator ++ collectBySettings(t)
        }
    }

    val tSettings =
      for {
        tGenre <- tGenres
        tOrder <- tOrders
        tSkip <- tSkips
      } yield Setting(tGenre, tOrder, tSkip)

    collectBySettings(tSettings.toList)
  }
}

object GenreSerializer extends StdSerializer[NovelGenre](classOf[NovelGenre]) {
  override def serialize(value: NovelGenre, gen: JsonGenerator, provider: SerializerProvider): Unit = {
    gen.writeNumber(value.getId)
  }
}

object NovelCollectorMain extends App {
  val tFile = new File("./novellist2")
  if (tFile.exists()) tFile.delete()

  val tNovelMap = OldNovelCollector.collect(NarouClientBuilder.init).take(10000).foldLeft(Map.empty[String, Novel]) {
    (m, n) => m.updated(n.getNcode, n)
  }

  println(s"取得ノベル数 ${tNovelMap.size}")
  val tWriter = new OutputStreamWriter(new FileOutputStream(tFile), StandardCharsets.UTF_8)
  val tMapper: ObjectMapper = new ObjectMapper
  tMapper.registerModule(new SimpleModule().tap(_.addSerializer(classOf[NovelGenre], GenreSerializer)))

  tNovelMap.values.foreach { tNovel =>
    tWriter.write(tMapper.writeValueAsString(tNovel))
    tWriter.write("\n")
  }

  tWriter.close()
}