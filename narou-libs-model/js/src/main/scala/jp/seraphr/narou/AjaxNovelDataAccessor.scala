package jp.seraphr.narou

import java.net.URI
import monix.eval.Task
import org.scalajs.dom.RequestInit
import scalajs.js

class AjaxNovelDataAccessor(aBaseUrl: URI) extends NovelDataAccessor {
  private val mExtractedMetaUrl = aBaseUrl.resolve(NovelFileNames.extractedMetaFile)
  private def metaUrl(aDir: String) = aBaseUrl.resolve(s"${aDir}/").resolve(NovelFileNames.metaFile)
  private def novelUrl(aDir: String, aFile: String) = aBaseUrl.resolve(s"${aDir}/").resolve(aFile)

  implicit class PromiseOps[A](p: => js.Promise[A]) {
    def toTask: Task[A] = Task.deferFuture(p.toFuture)
  }

  private def get(aUrl: URI): Task[String] = {
    import org.scalajs.dom
    dom.fetch(
      aUrl.toString,
      new RequestInit {
        method = dom.HttpMethod.GET
      }
    ).toTask.flatMap(_.text().toTask)
  }

  override val extractedMeta: Task[String] = get(mExtractedMetaUrl)
  override def metadata(aDir: String): Task[String] = get(metaUrl(aDir))
  override def getNovel(aDir: String, aFile: String): Task[String] = get(novelUrl(aDir, aFile))
}
