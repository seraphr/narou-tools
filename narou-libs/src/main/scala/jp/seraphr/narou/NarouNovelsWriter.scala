package jp.seraphr.narou

import jp.seraphr.narou.model.NarouNovel

trait NarouNovelsWriter extends AutoCloseable {
  def write(aNovel: NarouNovel): Unit
}