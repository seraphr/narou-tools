package jp.seraphr.narou

import java.io.{ BufferedWriter, File }
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import monix.eval.Task
import monix.reactive.Observable
import org.apache.commons.io.{ FileUtils => commonsFileUtils }

class FileNovelDataAccessor(aNovelDir: File) extends NovelDataAccessor {
  import FileUtils._

  private def extractedMetadataPath = (aNovelDir / NovelFileNames.extractedMetaFile).toPath

  override def writeExtractedMeta(aMetaString: String): Task[Unit] = Task {
    val tPath = extractedMetadataPath
    Files.createDirectories(tPath.getParent)
    Files.write(tPath, aMetaString.getBytes(StandardCharsets.UTF_8))
  }

  override val extractedMeta: Task[String] = Task {
    val tBytes = Files.readAllBytes(extractedMetadataPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }

  private def metadataPath(aDir: String) = (aNovelDir / aDir / NovelFileNames.metaFile).toPath

  override def backup(suffix: String): Task[Option[String]] = {
    for {
      tExists <- this.exists()
      tDestDir = aNovelDir.getParentFile / s"${aNovelDir.getName}${suffix}"
      _       <- Task.when(tExists) {
                   Task.eval {
                     val tDestDir = aNovelDir.getParentFile / s"${aNovelDir.getName}${suffix}"
                     commonsFileUtils.moveDirectory(aNovelDir, tDestDir)
                   }
                 }
    } yield Option.when(tExists)(tDestDir.toString)
  }

  override def exists(): Task[Boolean] = Task.eval(extractedMetadataPath.toFile.exists())

  override def writeMetadata(aDir: String, aMetaString: String): Task[Unit] = Task {
    val tPath = metadataPath(aDir)
    Files.createDirectories(tPath.getParent)
    Files.write(tPath, aMetaString.getBytes(StandardCharsets.UTF_8))
  }

  override def metadata(aDir: String): Task[String] = Task {
    val tBytes = Files.readAllBytes(metadataPath(aDir))
    new String(tBytes, StandardCharsets.UTF_8)
  }

  private def novelPath(aDir: String, aFile: String) = (aNovelDir / aDir / aFile).toPath

  override def writeNovel(aDir: String, aFile: String, aNovelStrings: Observable[String]): Task[Int] = {
    def newWriter(): BufferedWriter = {
      val tPath = novelPath(aDir, aFile)
      if (Files.exists(tPath)) {
        org.apache.commons.io.FileUtils.deleteQuietly(tPath.toFile)
      }
      Files.createDirectories(tPath.getParent)

      Files.newBufferedWriter(tPath)
    }

    for {
      tWriter <- Task.eval(newWriter())
      tCount  <- {
        aNovelStrings
          .map { tLine =>
            tWriter.write(tLine)
            tWriter.write("\n")
          }
          .countL
          .guarantee(Task.eval(tWriter.close()))
      }
    } yield tCount.toInt
  }

  override def getNovel(aDir: String, aFile: String): Observable[String] = Observable.defer {
    val tFile   = novelPath(aDir, aFile)
    val tReader = Task.eval(Files.newBufferedReader(tFile))

    Observable.fromLinesReader(tReader)
  }

}
