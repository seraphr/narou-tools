package jp.seraphr.narou

import java.io.{BufferedInputStream, BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import monix.eval.Task
import monix.reactive.Observable
import org.apache.commons.io.IOUtils

import java.nio.file.{Files, Path}

/**
 * @param aClient
 * @param aRootPath 書き込み先ディレクトリのパス名。 "/"終わりでも終わりじゃなくても良い
 */
class DropboxNovelDataAccessor(aClient: DbxClientV2, aRootPath: String) extends NovelDataAccessor {
  import jp.seraphr.narou.eval.TaskUtils._
  
  private val mRootPath = {
    val tPath = if (aRootPath.endsWith("/")) aRootPath.init else aRootPath
    if(tPath.startsWith("/")) tPath else "/" + tPath 
  }

  private def extractedMetadataPath                  = s"${mRootPath}/${NovelFileNames.extractedMetaFile}"
  private def metadataPath(aDir: String)             = s"${mRootPath}/${aDir}/${NovelFileNames.metaFile}"
  private def novelPath(aDir: String, aFile: String) = s"${mRootPath}/${aDir}/${aFile}"

  private def uploadString(aPath: String, aData: String): Task[Unit] = Task.eval[Unit] {
    aClient
      .files()
      .uploadBuilder(aPath)
      .withMode(WriteMode.ADD)
      .withAutorename(false)
      .uploadAndFinish(new ByteArrayInputStream(aData.getBytes(StandardCharsets.UTF_8)))
  }.retryBackoff()

  private def downloadString(aPath: String): Task[String] = Task.eval {
    val tByteArrayOutput = new ByteArrayOutputStream()
    aClient.files().download(aPath).download(tByteArrayOutput)

    tByteArrayOutput.toString(StandardCharsets.UTF_8)
  }.retryBackoff()

  override def backup(suffix: String): Task[Option[String]] = {
    for {
      tExists <- this.exists()
      tDestPath = mRootPath + suffix
      _ <- Task.when(tExists) {
        Task.eval[Unit] {
          aClient.files().moveV2(mRootPath, tDestPath)
        }.retryBackoff()
      }
    } yield Option.when(tExists)(tDestPath)
  }

  override def exists(): Task[Boolean] = Task.eval {
    import scala.jdk.CollectionConverters._
    aClient.files().listFolder(mRootPath).getEntries.asScala.exists(_.getName == NovelFileNames.extractedMetaFile)
  }.onErrorHandle(_ => false) // 存在しないディレクトリにlistFolderをすると例外が返る

  override def writeExtractedMeta(aMetaString: String): Task[Unit] = {
    println(s"extractedMetadataPath = ${extractedMetadataPath}")
    uploadString(extractedMetadataPath, aMetaString)
  }

  override def writeMetadata(aDir: String, aMetaString: String): Task[Unit] = {
    println(s"metadataPath(aDir) = ${metadataPath(aDir)}")
    uploadString(metadataPath(aDir), aMetaString)
  }

  override def writeNovel(aDir: String, aFile: String, aNovelStrings: Observable[String]): Task[Int] = Task.defer {
    val tTempFileTask = Task.eval(Files.createTempFile(aFile, ".jsonl"))
    def newWriter(aPath: Path) = Task.eval(Files.newBufferedWriter(aPath, StandardCharsets.UTF_8))
    def upload(aPath: Path): Task[Unit] = Task.eval[Unit] {
      val tInputStream = new BufferedInputStream(Files.newInputStream(aPath))
      
      try {
        aClient
          .files()
          .uploadBuilder(novelPath(aDir, aFile))
          .withMode(WriteMode.ADD)
          .withAutorename(false)
          .uploadAndFinish(tInputStream)
      } finally {
        IOUtils.closeQuietly(tInputStream)
      }
    }.retryBackoff()
    
    // dropboxのuploadAndFinishの代わりに getOutputStreamしてそこに書き込んだら、歯抜けにファイルが作られてしまった
    // 原因はわかっていないが、とりあえずtemp fileに書き込んでからそれをアップロードするようにした
    // もしエラーがあっても、これならretryが出来るのでちょっと安心
    val tWriteTempFileTask = for {
      tTempFile <- tTempFileTask
      tWriter <- newWriter(tTempFile)
      tCount <- {
        aNovelStrings
          .map { tLine =>
            tWriter.write(tLine)
            tWriter.write("\n")
          }
          .countL
          .guarantee(Task.eval(tWriter.close()))
      }
    } yield (tTempFile, tCount.toInt)
    
    for {
      tTempFileAndCount <- tWriteTempFileTask
      (tTempFile, tCount) = tTempFileAndCount
      _ <- upload(tTempFile).guarantee(Task.eval(Files.deleteIfExists(tTempFile)))
    } yield tCount
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
