package jp.seraphr.narou

import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta }
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.ArraySeq

object NovelFileNames {
  val extractedMetaFile = "extracted_novel_list.meta.json"
  val metaFile = "novel_list.meta.json"
  def novelFile(aIndex: Int) = s"novel_list_${aIndex}.jsonl"
}

trait NovelDataAccessor {
  val extractedMeta: Task[String]
  def metadata(aDir: String): Task[String]
  def getNovel(aFile: String): Task[String]
}

trait NovelLoader {
  val metadata: Task[NarouNovelsMeta]
  val novels: Observable[NarouNovel]
}

class DefaultNovelLoader(aAccessor: NovelDataAccessor) extends NovelLoader {
  import jp.seraphr.narou.json.NarouNovelFormats._
  import io.circe.parser.decode

  override val metadata: Task[NarouNovelsMeta] = aAccessor.metadata.flatMap(s => Task.fromEither(decode[NarouNovelsMeta](s)))

  override val novels: Observable[NarouNovel] = {
    Observable.fromTask(metadata)
      .flatMap(meta => Observable(meta.novelFiles: _*))
      .mapEval(aAccessor.getNovel)
      .flatMap(tFileBody => Observable(ArraySeq.unsafeWrapArray(tFileBody.split("\n")): _*))
      .mapEvalF(decode[NarouNovel])
  }
}
