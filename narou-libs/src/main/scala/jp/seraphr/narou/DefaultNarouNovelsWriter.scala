package jp.seraphr.narou

import java.util.Date

import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta }

import monix.eval.Task
import monix.reactive.Observable

class DefaultNarouNovelsWriter(aResultName: String, aWriter: NovelDataWriter, aDirName: String, aNovelPerFile: Int)
    extends NarouNovelsWriter {
  import jp.seraphr.narou.json.NarouNovelFormats._

  import io.circe.syntax._

  private val mWriter: NovelDataWriter = aWriter
  private def fileName(aIndex: Int)    = NovelFileNames.novelFile(aIndex)

  override def write(aNovels: Observable[NarouNovel]): Task[Unit] = {
    import jp.seraphr.narou.reactive.ObservableUtils._
    aNovels
      .grouped(aNovelPerFile)
      .mergeMap { case (tKey, tObs) =>
        val tFileName = fileName(tKey.toInt)
        Observable.fromTask(mWriter.writeNovel(aDirName, tFileName, tObs.map(_.asJson.noSpaces))).map((tFileName, _))
      }
      .toListL
      .flatMap { tList =>
        val (tFiles, tCounts) = tList.unzip
        val tMetaStr          = NarouNovelsMeta(aResultName, new Date(), tCounts.sum, tFiles.sorted).asJson.spaces2
        mWriter.writeMetadata(aDirName, tMetaStr)
      }
  }

}
