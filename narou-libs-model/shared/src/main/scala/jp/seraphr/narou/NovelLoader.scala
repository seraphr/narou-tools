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
  val allMetadata: Task[Seq[NarouNovelsMeta]]
  def loader(aDir: String): Task[DefaultNovelLoader]
  def load(aDir: String): Observable[NarouNovel] = {
    Observable.fromTask(loader((aDir))).flatMap(_.novels)
  }

  lazy val loadAll: Observable[NarouNovel] = {
    Observable.fromTask(metadata)
      .flatMap(m => Observable.fromIterable(m.conditionDirs))
      .flatMap(this.load)
  }
}

import jp.seraphr.narou.json.NarouNovelFormats._
import io.circe.parser.decode

class DefaultExtractedNovelLoader(aAccessor: NovelDataAccessor) extends ExtractedNovelLoader {
  override val metadata: Task[ExtractNarouNovelsMeta] = aAccessor.extractedMeta.flatMap(s => Task.fromEither(decode[ExtractNarouNovelsMeta](s)))
  override val allMetadata: Task[Seq[NarouNovelsMeta]] = {
    for {
      tMeta <- metadata
      tLoaders <- Task.traverse(tMeta.conditionDirs)(this.loader)
      tNovelsMetas <- Task.traverse(tLoaders)(_.metadata)
    } yield tNovelsMetas
  }

  override def loader(aDir: String): Task[DefaultNovelLoader] = Task.now {
    new DefaultNovelLoader(aAccessor, aDir)
  }
}

trait NovelLoader {
  val metadata: Task[NarouNovelsMeta]
  val novels: Observable[NarouNovel]
}

class DefaultNovelLoader(aAccessor: NovelDataAccessor, aExtractedDir: String) extends NovelLoader {

  override val metadata: Task[NarouNovelsMeta] = aAccessor.metadata(aExtractedDir).flatMap(s => Task.fromEither(decode[NarouNovelsMeta](s)))

  override val novels: Observable[NarouNovel] = {
    Observable.fromTask(metadata)
      .flatMap(meta => Observable(meta.novelFiles: _*))
      .mapEval(aAccessor.getNovel(aExtractedDir, _))
      .flatMap(tFileBody => Observable(ArraySeq.unsafeWrapArray(tFileBody.split("\n")): _*))
      .mapEvalF(decode[NarouNovel])
  }
}
