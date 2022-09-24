package jp.seraphr.narou

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import monix.eval.Task

class FileNovelDataReader(aNovelDir: File) extends NovelDataReader {
  import FileUtils._

  override val extractedMeta: Task[String] = Task {
    val tBytes = Files.readAllBytes((aNovelDir / NovelFileNames.extractedMetaFile).toPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }

  override def metadata(aDir: String): Task[String] = Task {
    val tBytes = Files.readAllBytes((aNovelDir / aDir / NovelFileNames.metaFile).toPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }

  override def getNovel(aDir: String, aFile: String): Task[String] = Task {
    val tFile  = aNovelDir / aDir / aFile
    val tBytes = Files.readAllBytes(tFile.toPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }

}
