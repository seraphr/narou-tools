package jp.seraphr.narou

import java.io.File

object FileUtils {
  implicit class FileOps(file: File) {
    def /(aChild: String)            = new File(file, aChild)
    def modName(f: String => String) = new File(file.getParentFile, f(file.getName))
  }
}
