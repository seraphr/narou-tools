package jp.seraphr.narou

import java.io.{ Closeable, File }

import jp.seraphr.narou.model.{ NarouNovel, NovelCondition }
import org.apache.commons.io.IOUtils

class ExtractedNarouNovelsWriter(aBaseDir: File, aConditions: Seq[NovelCondition], aNovelsPerFile: Int) extends NarouNovelsWriter {
  private val mConditions = {
    if (aConditions.contains(NovelCondition.all)) aConditions
    else NovelCondition.all +: aConditions
  }

  class ConditionWriter(aCondition: NovelCondition) extends NarouNovelsWriter with Closeable {
    import FileUtils._
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
    mDefaultWriters.foreach(w => IOUtils.closeQuietly(w))
  }
}

