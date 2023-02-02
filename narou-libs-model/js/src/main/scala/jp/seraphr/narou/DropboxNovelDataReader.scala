package jp.seraphr.narou

import org.scalajs.dom.Blob
import scala.scalajs.js

import monix.eval.Task
import monix.reactive.Observable
import typings.dropbox.mod.Dropbox
import typings.dropbox.typesDropboxTypesMod.files.{ DownloadArg, FileMetadata, ListFolderArg, Metadata }

@js.native
trait DropboxDownloadFileMetadata extends FileMetadata {
  // see https://github.com/dropbox/dropbox-sdk-js/blob/dcc134dda8d39329c518ba66c2ac1ff187e43bde/src/response.js#L43-L64

  /** ブラウザ内の環境の時、こちらの値が入る */
  val fileBlob: js.UndefOr[Blob]

  /** fileBlobが空の時、こちらに値が入る。 実際の型はnodejsのBuffer型。 bindingライブラリ入れてないのでjs.Anyにしておく */
  val fileBinary: js.UndefOr[js.Any]
}

class DropboxNovelDataReader(aClient: Dropbox, aRootPath: String) extends NovelDataReader {
  import jp.seraphr.narou.eval.TaskUtils._

  private val mRootPath = {
    val tPath = if (aRootPath.endsWith("/")) aRootPath.init else aRootPath
    if (tPath.startsWith("/")) tPath else "/" + tPath
  }

  private def extractedMetadataPath                  = s"${mRootPath}/${NovelFileNames.extractedMetaFile}"
  private def metadataPath(aDir: String)             = s"${mRootPath}/${aDir}/${NovelFileNames.metaFile}"
  private def novelPath(aDir: String, aFile: String) = s"${mRootPath}/${aDir}/${aFile}"

  private def downloadString(aPath: String): Task[String] = {
    for {
      tResponse        <- Task.deferFuture(aClient.filesDownload(DownloadArg(aPath)).toFuture)
      tDownloadResponse = tResponse.result.asInstanceOf[DropboxDownloadFileMetadata]
      tBlob             = tDownloadResponse.fileBlob.getOrElse(throw new RuntimeException("fileBlobが見つかりません"))
      tText            <- Task.deferFuture(tBlob.text().toFuture)
    } yield tText
  }.retryBackoff()

  override def exists(): Task[Boolean] = {
    for {
      tListFolder <- Task.deferFuture(aClient.filesListFolder(ListFolderArg(mRootPath)).toFuture)
      tExists      = tListFolder.result.entries.exists(_.asInstanceOf[Metadata].name == NovelFileNames.extractedMetaFile)
    } yield tExists
  }.onErrorHandle(_ => false) // 存在しないディレクトリにlistFolderをすると例外が返る

  override val extractedMeta: Task[String] = {
    downloadString(extractedMetadataPath)
  }

  override def metadata(aDir: String): Task[String] = {
    downloadString(metadataPath(aDir))
  }

  override def getNovel(aDir: String, aFile: String): Observable[String] = {
    // XXX Streaming処理したいがブラウザでreadlineするの地味に面倒そうなのでとりあえずやめた
    for {
      tText <- Observable.fromTask(downloadString(novelPath(aDir, aFile)))
      tLine <- Observable.fromIterator(Task.eval(tText.linesIterator))
    } yield tLine
  }

}
