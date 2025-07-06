package jp.seraphr.narou.api

import jp.seraphr.narou.api.model.{ NovelApiResponse, SearchParams }

import monix.eval.Task

/** なろう小説APIクライアント */
trait NarouApiClient {

  /**
   * 小説を検索する
   * @param params 検索パラメータ
   * @return 検索結果
   */
  def search(params: SearchParams): Task[NovelApiResponse]

  /**
   * 小説を検索する（簡易版）
   * @param word 検索キーワード
   * @param limit 取得件数（デフォルト20）
   * @return 検索結果
   */
  def searchByWord(word: String, limit: Int = 20): Task[NovelApiResponse] = {
    search(SearchParams(word = Some(word), lim = Some(limit)))
  }

  /**
   * 特定のNコードで小説を取得する
   * @param ncode Nコード
   * @return 小説情報
   */
  def getByNcode(ncode: String): Task[NovelApiResponse] = {
    search(SearchParams(ncode = Some(ncode)))
  }

}

/** ファクトリメソッド */
object NarouApiClient {

  /**
   * デフォルトの実装を作成する
   * プラットフォーム固有の実装は各プラットフォームで提供される
   */
  def apply(): Task[NarouApiClient] = NarouApiClientPlatform.create()
}
