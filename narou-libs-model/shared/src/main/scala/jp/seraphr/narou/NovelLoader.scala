package jp.seraphr.narou

import scala.collection.immutable.ArraySeq

import jp.seraphr.narou.model.{ ExtractedNarouNovelsMeta, NarouNovel, NarouNovelsMeta }

import monix.eval.Task
import monix.reactive.Observable

object NovelFileNames {
  val extractedMetaFile      = "extracted_novel_list.meta.json"
  val metaFile               = "novel_list.meta.json"
  def novelFile(aIndex: Int) = s"novel_list_${aIndex}.jsonl"
}

/**
 * NarouNovelの文字列表現の読み込みを行う
 * 文字列化した情報の読み込み元の抽象化を行うためのレイヤー
 */
trait NovelDataReader {

  /**
   * [[metadata]]や[[getNovel]]の引数情報を保持する情報を取得する
   * [[ExtractedNarouNovelsMeta]]の文字列表現を想定している
   */
  val extractedMeta: Task[String]

  /**
   * aDirに存在する小説のメタデータ情報を取得する
   * [[NarouNovelsMeta]]の文字列表現を想定している
   */
  def metadata(aDir: String): Task[String]

  /**
   * aDir/aFileに存在する小説情報を取得する
   * [[NarouNovel]]の[[Seq]]の文字列表現を想定している
   */
  def getNovel(aDir: String, aFile: String): Task[String]
}

trait ExtractedNovelLoader {
  val metadata: Task[ExtractedNarouNovelsMeta]

  /** ディレクトリ名 -> NarouNovelsMetaのMap */
  val allMetadata: Task[Map[String, NarouNovelsMeta]]
  def loader(aDir: String): Task[NovelLoader]
  def load(aDir: String): Observable[NarouNovel] = {
    Observable.fromTask(loader(aDir)).flatMap(_.novels)
  }

  lazy val loadAll: Observable[NarouNovel] = {
    Observable.fromTask(metadata).flatMap(m => Observable.fromIterable(m.conditionDirs)).flatMap(this.load)
  }

}

import jp.seraphr.narou.json.NarouNovelFormats._

import io.circe.parser.decode

class DefaultExtractedNovelLoader(aAccessor: NovelDataReader) extends ExtractedNovelLoader {
  override val metadata: Task[ExtractedNarouNovelsMeta] = aAccessor
    .extractedMeta
    .flatMap(s => Task.fromEither(decode[ExtractedNarouNovelsMeta](s)))

  override val allMetadata: Task[Map[String, NarouNovelsMeta]] = {
    for {
      tMeta        <- metadata
      tLoaders     <- Task.traverse(tMeta.conditionDirs)(this.loader)
      tNovelsMetas <- Task.traverse(tLoaders)(_.metadataWithDir)
    } yield tNovelsMetas.toMap
  }

  override def loader(aDir: String): Task[DefaultNovelLoader] = Task.now {
    new DefaultNovelLoader(aAccessor, aDir)
  }

}

trait NovelLoader {
  val metadata: Task[NarouNovelsMeta]
  val novels: Observable[NarouNovel]
}

class DefaultNovelLoader(aAccessor: NovelDataReader, aExtractedDir: String) extends NovelLoader {

  val dir                                      = aExtractedDir
  override val metadata: Task[NarouNovelsMeta] = aAccessor
    .metadata(aExtractedDir)
    .flatMap(s => Task.fromEither(decode[NarouNovelsMeta](s)))

  val metadataWithDir: Task[(String, NarouNovelsMeta)] = metadata.map(dir -> _)

  override val novels: Observable[NarouNovel] = {
    Observable
      .fromTask(metadata)
      .flatMap(meta => Observable(meta.novelFiles: _*))
      .mapEval(aAccessor.getNovel(aExtractedDir, _))
      .flatMap(tFileBody => Observable(ArraySeq.unsafeWrapArray(tFileBody.split("\n")): _*))
      .mapEvalF(decode[NarouNovel])
  }

}
