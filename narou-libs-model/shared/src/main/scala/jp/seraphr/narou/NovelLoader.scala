package jp.seraphr.narou

import jp.seraphr.narou.model.{ ExtractNarouNovelsMeta, NarouNovel, NarouNovelsMeta }
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
  def getNovel(aDir: String, aFile: String): Task[String]
}

trait ExtractedNovelLoader {
  val metadata: Task[ExtractNarouNovelsMeta]
}

trait NovelLoader {
  val metadata: Task[NarouNovelsMeta]
  val novels: Observable[NarouNovel]
}

class DefaultNovelLoader(aAccessor: NovelDataAccessor, aExtractedDir: String) extends NovelLoader {
  import jp.seraphr.narou.json.NarouNovelFormats._
  import io.circe.parser.decode

  override val metadata: Task[NarouNovelsMeta] = aAccessor.metadata(aExtractedDir).flatMap(s => Task.fromEither(decode[NarouNovelsMeta](s)))

  override val novels: Observable[NarouNovel] = {
    Observable.fromTask(metadata)
      .flatMap(meta => Observable(meta.novelFiles: _*))
      .mapEval(aAccessor.getNovel(aExtractedDir, _))
      .flatMap(tFileBody => Observable(ArraySeq.unsafeWrapArray(tFileBody.split("\n")): _*))
      .mapEvalF(decode[NarouNovel])
  }
}
