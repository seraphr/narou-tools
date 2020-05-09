package jp.seraphr.narou

import java.net.URI

import monix.eval.Task
import org.scalajs.dom.ext.Ajax

class AjaxNovelDataAccessor(aBaseUrl: URI) extends NovelDataAccessor {
  private val mExtractedMetaUrl = aBaseUrl.resolve(NovelFileNames.extractedMetaFile)
  private def metaUrl(aDir: String) = aBaseUrl.resolve(s"${aDir}/").resolve(NovelFileNames.metaFile)
  private def novelUrl(aDir: String, aFile: String) = aBaseUrl.resolve(s"${aDir}/").resolve(aFile)

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

  override val extractedMeta: Task[String] = get(mExtractedMetaUrl)
  override def metadata(aDir: String): Task[String] = get(metaUrl(aDir))
  override def getNovel(aDir: String, aFile: String): Task[String] = get(novelUrl(aDir, aFile))
}
