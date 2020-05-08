package jp.seraphr.narou

import java.net.URI

import monix.eval.Task
import org.scalajs.dom.ext.Ajax

class AjaxNovelDataAccessor(aBaseUrl: URI) extends NovelDataAccessor {
  private val mMetaUrl = aBaseUrl.resolve(NovelFileNames.metaFile)
  private def novelUrl(aFile: String) = aBaseUrl.resolve(aFile)

  private def get(aUrl: URI): Task[String] = {
    Task.defer(
      Task.fromFuture(
        Ajax.get(
          aUrl.toString,
          data = null,
          timeout = 100000,
          withCredentials = false,
          responseType = "text"
        )
      )
    ).map(_.responseText)

  }

  override def metadata: Task[String] = get(mMetaUrl)
  override def getNovel(aFile: String): Task[String] = get(novelUrl(aFile))
}
