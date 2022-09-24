package jp.seraphr.narou

import java.io.File
import java.util.Date

import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta }

import monix.eval.Task
import monix.reactive.Observable

class DefaultNarouNovelsWriter(aResultName: String, aDir: File, aNovelPerFile: Int) extends NarouNovelsWriter {
  import jp.seraphr.narou.json.NarouNovelFormats._

  import io.circe.syntax._

  if (aDir.exists()) {
    org.apache.commons.io.FileUtils.deleteQuietly(aDir)
  }
  aDir.mkdirs()

  // TODO NovelDataWriterは外からもらうようにする
  private val mWriter: NovelDataWriter = new FileNovelDataAccessor(aDir.getParentFile)
  private def fileName(aIndex: Int)    = NovelFileNames.novelFile(aIndex)

  override def write(aNovels: Observable[NarouNovel]): Task[Unit] = {
    import jp.seraphr.narou.reactive.ObservableUtils._
    aNovels
      .grouped(aNovelPerFile)
      .mergeMap { case (tKey, tObs) =>
        val tFileName = fileName(tKey.toInt)
        Observable.fromTask(mWriter.writeNovel(aDir.getName, tFileName, tObs.map(_.asJson.noSpaces))).map((tFileName, _))
      }
      .toListL
      .foreachL { tList =>
        val (tFiles, tCounts) = tList.unzip
        val tMetaStr          = NarouNovelsMeta(aResultName, new Date(), tCounts.sum, tFiles).asJson.spaces2
        mWriter.writeMetadata(aDir.getName, tMetaStr)
      }
  }

}
