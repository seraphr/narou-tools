package jp.seraphr.narou.api

import jp.seraphr.narou.api.model.{ NovelApiResponse, NovelBody, SearchParams }

import monix.eval.Task

/** なろう小説APIクライアント */
trait NarouApiClient {

  /**
   * 小説を検索する
   * @param aParams 検索パラメータ
   * @return 検索結果
   */
  def search(aParams: SearchParams): Task[NovelApiResponse]

  /**
   * 小説を検索する（簡易版）
   * @param aWord 検索キーワード
   * @param aLimit 取得件数（デフォルト20）
   * @return 検索結果
   */
  def searchByWord(aWord: String, aLimit: Int = 20): Task[NovelApiResponse] = {
    search(SearchParams(word = Some(aWord), lim = Some(aLimit)))
  }

  /**
   * 特定のNコードで小説を取得する
   * @param aNcode Nコード
   * @return 小説情報
   */
  def getByNcode(aNcode: String): Task[NovelApiResponse] = {
    search(SearchParams(ncode = Some(aNcode)))
  }

  /**
   * 指定した小説の目次を取得する
   * @param aNcode Nコード
   * @return 目次リスト
   */
  def getNovelTable(aNcode: String): Task[List[NovelBody]]

  /**
   * 指定した小説の指定ページの本文を取得する
   * @param aNcode Nコード
   * @param aPage ページ番号
   * @return 本文情報
   */
  def getNovelBody(aNcode: String, aPage: Int): Task[NovelBody]

}

/** ファクトリメソッド */
object NarouApiClient {

  /**
   * デフォルトの実装を作成する
   * プラットフォーム固有の実装は各プラットフォームで提供される
   */
  def apply(): Task[NarouApiClient] = NarouApiClientPlatform.create()
}
