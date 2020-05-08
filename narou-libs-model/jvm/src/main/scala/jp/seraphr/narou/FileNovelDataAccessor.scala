package jp.seraphr.narou
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import monix.eval.Task

class FileNovelDataAccessor(aNovelDir: File) extends NovelDataAccessor {
  import FileUtils._
  override def metadata: Task[String] = Task {
    val tBytes = Files.readAllBytes((aNovelDir / NovelFileNames.metaFile).toPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }

  override def getNovel(aFile: String): Task[String] = Task {
    val tFile = aNovelDir / aFile
    val tBytes = Files.readAllBytes(tFile.toPath)
    new String(tBytes, StandardCharsets.UTF_8)
  }
}
