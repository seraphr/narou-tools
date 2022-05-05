package jp.seraphr.narou
import jp.seraphr.narou.model.{ ExtractedNarouNovelsMeta, NarouNovel, NarouNovelsMeta }
import monix.eval.Task
import monix.reactive.Observable

import java.util.Date

class DummyExtractedNovelLoader(data: Map[String, NovelLoader]) extends ExtractedNovelLoader {
  override val metadata: Task[ExtractedNarouNovelsMeta] = Task.pure(
    ExtractedNarouNovelsMeta(data.keys.toVector)
  )

  override val allMetadata: Task[Map[String, NarouNovelsMeta]] = Task.traverse(data.toSeq) {
    case (tDir, tLoader) => tLoader.metadata.map(tDir -> _)
  }.map(_.toMap)
  override def loader(aDir: String): Task[NovelLoader] = Task.pure(data(aDir))
}

class DummyNovelLoader(name: String, data: Seq[NarouNovel]) extends NovelLoader {
  override val metadata: Task[NarouNovelsMeta] = Task.pure(NarouNovelsMeta(
    name = name,
    createdAt = new Date(),
    data.size,
    Seq("dummyFile")
  ))
  override val novels: Observable[NarouNovel] = Observable.fromIterable(data)
}
