package jp.seraphr.narou

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, StandardOpenOption }

import jp.seraphr.narou.model.{ ExtractedNarouNovelsMeta, NarouNovel, NovelCondition }

import monix.eval.Task
import monix.reactive.Observable

class ExtractedNarouNovelsWriter(aBaseDir: File, aConditions: Seq[NovelCondition], aNovelsPerFile: Int)
    extends NarouNovelsWriter {
  import FileUtils._
  private val mConditions = {
    if (aConditions.contains(NovelCondition.all)) aConditions
    else NovelCondition.all +: aConditions
  }

  class ConditionWriter(aCondition: NovelCondition) extends NarouNovelsWriter {
    private val mWriter = new DefaultNarouNovelsWriter(aCondition.name, aBaseDir / aCondition.id, aNovelsPerFile)

    override def write(aNovels: Observable[NarouNovel]): Task[Unit] = {
      mWriter.write(aNovels.filter(aCondition.predicate))
    }

  }
  private val mDefaultWriters = mConditions.map { new ConditionWriter(_) }

  override def write(aNovels: Observable[NarouNovel]): Task[Unit] = {
    aNovels
      .publishSelector { tHotObservable =>
        Observable(mDefaultWriters.map { tWriter =>
          Observable.fromTask(tWriter.write(tHotObservable))
        }: _*).merge
      }
      .completedL
      .doOnFinish {
        case Some(_) => Task.unit
        case None    =>
          // 成功時、メタデータを保存する
          import jp.seraphr.narou.json.NarouNovelFormats._
          import io.circe.syntax._

          val tMetaStr  = ExtractedNarouNovelsMeta(aConditions.map(_.id)).asJson.spaces2
          val tMetaFile = aBaseDir / NovelFileNames.extractedMetaFile
          Task.eval {
            Files.write(
              tMetaFile.toPath,
              tMetaStr.getBytes(StandardCharsets.UTF_8),
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.CREATE,
              StandardOpenOption.WRITE
            )
          }
      }
  }

}
