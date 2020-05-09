package jp.seraphr.narou

import java.io.{ Closeable, File }
import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, StandardOpenOption }

import jp.seraphr.narou.model.{ ExtractNarouNovelsMeta, NarouNovel, NovelCondition }
import org.apache.commons.io.IOUtils

class ExtractedNarouNovelsWriter(aBaseDir: File, aConditions: Seq[NovelCondition], aNovelsPerFile: Int) extends NarouNovelsWriter {
  import FileUtils._
  private val mConditions = {
    if (aConditions.contains(NovelCondition.all)) aConditions
    else NovelCondition.all +: aConditions
  }

  class ConditionWriter(aCondition: NovelCondition) extends NarouNovelsWriter with Closeable {
    private val mWriter = new DefaultNarouNovelsWriter(aCondition.name, aBaseDir / aCondition.id, aNovelsPerFile)

    override def write(aNovel: NarouNovel): Unit = {
      if (aCondition.predicate(aNovel)) {
        mWriter.write(aNovel)
      }
    }

    override def close(): Unit = mWriter.close()
  }
  private val mDefaultWriters = mConditions.map { new ConditionWriter(_) }

  override def write(aNovel: NarouNovel): Unit = {
    mDefaultWriters.foreach(_.write(aNovel))
  }

  override def close(): Unit = {
    import jp.seraphr.narou.json.NarouNovelFormats._
    import io.circe.syntax._

    val tMetaStr = ExtractNarouNovelsMeta(aConditions.map(_.id)).asJson.spaces2
    val tMetaFile = aBaseDir / NovelFileNames.extractedMetaFile
    Files.write(tMetaFile.toPath, tMetaStr.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE)

    mDefaultWriters.foreach(w => IOUtils.closeQuietly(w))
  }
}

