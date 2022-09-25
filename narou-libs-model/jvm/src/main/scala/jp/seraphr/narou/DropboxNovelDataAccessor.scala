package jp.seraphr.narou

import java.io.{ BufferedReader, ByteArrayInputStream, ByteArrayOutputStream }
import java.nio.charset.StandardCharsets

import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import jdk.internal.org.jline.utils.InputStreamReader
import monix.eval.Task
import monix.reactive.Observable
import org.apache.commons.io.IOUtils

/**
 * @param aClient
 * @param aRootPath 書き込み先ディレクトリのパス名。 "/"終わりでも終わりじゃなくても良い
 */
class DropboxNovelDataAccessor(aClient: DbxClientV2, aRootPath: String) extends NovelDataAccessor {
  private val mRootPath = if (aRootPath.endsWith("/")) aRootPath.init else aRootPath

  private def extractedMetadataPath                  = s"${mRootPath}/${NovelFileNames.extractedMetaFile}"
  private def metadataPath(aDir: String)             = s"${mRootPath}/${aDir}/${NovelFileNames.metaFile}"
  private def novelPath(aDir: String, aFile: String) = s"${mRootPath}/${aDir}/${aFile}"

  private def uploadString(aPath: String, aData: String): Task[Unit] = Task.eval {
    aClient
      .files()
      .uploadBuilder(aPath)
      .withMode(WriteMode.ADD)
      .withAutorename(false)
      .uploadAndFinish(new ByteArrayInputStream(aData.getBytes(StandardCharsets.UTF_8)))
  }

  private def downloadString(aPath: String): Task[String] = Task.eval {
    val tByteArrayOutput = new ByteArrayOutputStream()
    aClient.files().download(aPath).download(tByteArrayOutput)

    tByteArrayOutput.toString(StandardCharsets.UTF_8)
  }

  override def exists(): Task[Boolean] = Task.eval {
    import scala.jdk.CollectionConverters._
    aClient.files().listFolder(mRootPath).getEntries.asScala.exists(_.getName == NovelFileNames.extractedMetaFile)
  }

  override def writeExtractedMeta(aMetaString: String): Task[Unit] = {
    uploadString(extractedMetadataPath, aMetaString)
  }

  override def writeMetadata(aDir: String, aMetaString: String): Task[Unit] = {
    uploadString(metadataPath(aDir), aMetaString)
  }

  override def writeNovel(aDir: String, aFile: String, aNovelStrings: Observable[String]): Task[Int] = Task.defer {
    val tOutputStream = aClient
      .files()
      .uploadBuilder(novelPath(aDir, aFile))
      .withMode(WriteMode.ADD)
      .withAutorename(false)
      .start()
      .getOutputStream()

    aNovelStrings
      .map { tLine =>
        tOutputStream.write(tLine.getBytes(StandardCharsets.UTF_8))
        tOutputStream.write('\n')
      }
      .countL
      .map(_.toInt)
      .guarantee(Task.eval(IOUtils.closeQuietly(tOutputStream)))
  }

  override val extractedMeta: Task[String] = {
    downloadString(extractedMetadataPath)
  }

  override def metadata(aDir: String): Task[String] = {
    downloadString(metadataPath(aDir))
  }

  override def getNovel(aDir: String, aFile: String): Observable[String] = {
    val tReaderTask = Task.eval {
      val tInput = aClient.files().download(novelPath(aDir, aFile)).getInputStream

      new BufferedReader(new InputStreamReader(tInput, StandardCharsets.UTF_8))
    }

    Observable.fromLinesReader(tReaderTask)
  }

}
