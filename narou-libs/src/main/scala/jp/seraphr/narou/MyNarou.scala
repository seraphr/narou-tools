package jp.seraphr.narou

import jp.seraphr.narou.api.NarouApiClient
import jp.seraphr.narou.api.model.NovelBody

import monix.eval.Task
import monix.execution.Scheduler

/** 小説本文取得のためのクライアント */
class MyNarou {
  implicit val scheduler: Scheduler = Scheduler.global
  private val client                = NarouApiClient().runSyncUnsafe()

  /**
   * 指定された小説の指定ページの本文を取得する
   *
   * @param ncode 小説コード
   * @param page ページ番号（1以上）
   * @return 小説本文データ
   */
  def getNovelBody(ncode: String, page: Int): Task[NovelBody] = {
    if (page <= 0) Task.raiseError(new RuntimeException("pageは1以上である必要があります"))
    else client.getNovelBody(ncode, page)
  }

}
