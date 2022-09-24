package jp.seraphr.narou

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import monix.eval.Task

class FileNovelDataAccessor(aNovelDir: File) extends NovelDataAccessor {
  import FileUtils._

  private def extractedMetaDataPath = (aNovelDir / NovelFileNames.extractedMetaFile).toPath

  override def writeExtractedMeta(aMetaString: String): Task[Unit] = Task {
    Files.write(extractedMetaDataPath, aMetaString.getBytes(StandardCharsets.UTF_8))
  }

  override val extractedMeta: Task[String] = Task {
    val tBytes = Files.readAllBytes(extractedMetaDataPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }

  private def metadataPath(aDir: String) = (aNovelDir / aDir / NovelFileNames.metaFile).toPath

  override def writeMetadata(aDir: String, aMetaString: String): Task[Unit] = Task {
    Files.write(metadataPath(aDir), aMetaString.getBytes(StandardCharsets.UTF_8))
  }

  override def metadata(aDir: String): Task[String] = Task {
    val tBytes = Files.readAllBytes(metadataPath(aDir))
    new String(tBytes, StandardCharsets.UTF_8)
  }

  private def novelPath(aDir: String, aFile: String) = (aNovelDir / aDir / aFile).toPath

  override def writeNovel(aDir: String, aFile: String, aNovelString: String): Task[Unit] = Task {
    Files.write(novelPath(aDir, aFile), aNovelString.getBytes(StandardCharsets.UTF_8))
  }

  override def getNovel(aDir: String, aFile: String): Task[String] = Task {
    val tFile  = aNovelDir / aDir / aFile
    val tBytes = Files.readAllBytes(tFile.toPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }

}
