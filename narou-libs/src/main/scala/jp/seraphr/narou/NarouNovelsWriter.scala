package jp.seraphr.narou

import jp.seraphr.narou.model.NarouNovel

import monix.eval.Task
import monix.reactive.Observable

trait NarouNovelsWriter {
  def write(aNovels: Observable[NarouNovel]): Task[Unit]
}
