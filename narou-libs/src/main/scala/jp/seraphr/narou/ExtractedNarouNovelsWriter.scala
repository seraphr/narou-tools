package jp.seraphr.narou

import jp.seraphr.narou.model.{ ExtractedNarouNovelsMeta, NarouNovel, NovelCondition }

import monix.eval.Task
import monix.reactive.Observable

class ExtractedNarouNovelsWriter(aDataWriter: NovelDataWriter, aConditions: Seq[NovelCondition], aNovelsPerFile: Int)
    extends NarouNovelsWriter {
  private val mConditions = aConditions

  class ConditionWriter(aCondition: NovelCondition) extends NarouNovelsWriter {
    private val mWriter = new DefaultNarouNovelsWriter(aCondition.name, aDataWriter, aCondition.id, aNovelsPerFile)

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
        }: _*).mergeMap(identity) // mergeと等価なんだけど、scala 3.3.5 にしたら何故かコンパイルが通らん…
      }
      .completedL
      .doOnFinish {
        case Some(_) => Task.unit
        case None    =>
          // 成功時、メタデータを保存する
          import jp.seraphr.narou.json.NarouNovelFormats._
          import io.circe.syntax._

          val tMetaStr = ExtractedNarouNovelsMeta(aConditions.map(_.id)).asJson.spaces2
          aDataWriter.writeExtractedMeta(tMetaStr)
      }
  }

}
