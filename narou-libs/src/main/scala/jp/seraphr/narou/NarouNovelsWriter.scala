package jp.seraphr.narou

import java.io.{ BufferedWriter, File }
import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, StandardOpenOption }
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta }
import org.apache.commons.io.IOUtils

class NarouNovelsWriter(aDir: File, aNovelPerFile: Int) extends AutoCloseable {
  import FileUtils._
  import jp.seraphr.narou.json.NarouNovelFormats._
  import io.circe.syntax._

  if (aDir.exists()) {
    org.apache.commons.io.FileUtils.deleteQuietly(aDir)
  }
  aDir.mkdirs()

  private val mMetaFile = aDir / "novel_list.meta.json"
  private def fileName(aIndex: Int) = s"novel_list_${aIndex}.jsonl"
  private def novelFile(aIndex: Int) = aDir / fileName(aIndex)
  private def fileCount = {
    val tCount = mNovelCount.get() / aNovelPerFile
    if (mNovelCount.get() % aNovelPerFile == 0) tCount
    else tCount + 1
  }
  private def novelList = (0 until fileCount).map(fileName).toList
  private val mNovelCount = new AtomicInteger(0)

  private var mWriter: BufferedWriter = null
  private def newWriter(): BufferedWriter = {
    val tFile = novelFile((mNovelCount.get + 1) / aNovelPerFile)
    Files.newBufferedWriter(tFile.toPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)
  }
  private def writer(): BufferedWriter = {
    if (mWriter == null) {
      mWriter = newWriter()
      return mWriter
    }

    if (mNovelCount.get() % aNovelPerFile == 0) {
      IOUtils.closeQuietly(mWriter)
      mWriter = newWriter()
    }

    mWriter
  }

  def write(aNovel: NarouNovel): Unit = {
    val tWriter = writer()
    tWriter.write(aNovel.asJson.noSpaces)
    tWriter.write("\n")
    mNovelCount.getAndIncrement()
  }

  override def close(): Unit = {
    if (mWriter != null) {
      mWriter.close()
    }

    val tMetaStr = NarouNovelsMeta(new Date(), mNovelCount.get(), novelList).asJson.spaces2
    Files.write(mMetaFile.toPath, tMetaStr.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
  }
}
