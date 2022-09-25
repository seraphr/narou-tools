package jp.seraphr.narou

import java.net.URI

import org.scalajs.dom.RequestInit

import monix.eval.Task
import monix.reactive.Observable
import scalajs.js

class AjaxNovelDataReader(aBaseUrl: URI) extends NovelDataReader {
  private val mExtractedMetaUrl                     = aBaseUrl.resolve(NovelFileNames.extractedMetaFile)
  private def metaUrl(aDir: String)                 = aBaseUrl.resolve(s"${aDir}/").resolve(NovelFileNames.metaFile)
  private def novelUrl(aDir: String, aFile: String) = aBaseUrl.resolve(s"${aDir}/").resolve(aFile)

  implicit class PromiseOps[A](p: => js.Promise[A]) {
    def toTask: Task[A] = Task.deferFuture(p.toFuture)
  }

  private def get(aUrl: URI): Task[String] = {
    import org.scalajs.dom
    dom
      .fetch(
        aUrl.toString,
        new RequestInit {
          method = dom.HttpMethod.GET
        }
      )
      .toTask
      .flatMap(_.text().toTask)
  }

  override def exists(): Task[Boolean]     = Task.raiseError(new RuntimeException("AjaxNovelDataReaderでは、existsは現状未実装です"))

  override val extractedMeta: Task[String]                               = get(mExtractedMetaUrl)
  override def metadata(aDir: String): Task[String]                      = get(metaUrl(aDir))
  override def getNovel(aDir: String, aFile: String): Observable[String] = {
    for {
      tString <- Observable.fromTask(get(novelUrl(aDir, aFile)))
      tLine   <- Observable.fromIterable(tString.split('\n'))
    } yield tLine
  }

}
