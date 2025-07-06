package jp.seraphr.narou.api

import scala.concurrent.duration._

import jp.seraphr.narou.api.model.SearchParams

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class NarouApiClientIntegrationTest extends AsyncFreeSpec with Matchers with BeforeAndAfterAll {

  var clientOpt: Option[NarouApiClient] = None

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  private val client: Task[NarouApiClient] = NarouApiClientPlatform.create()

  "NarouApiClientImpl統合テスト" - {

    "基本的な検索でJSONレスポンスを取得できること" in {
      // 最小限のリクエスト（1件のみ）
      val params = SearchParams(lim = Some(1))

      for {
        client <- this.client
        _      <- client
                    .search(params)
                    .map {
                      result =>
                        // APIレスポンスの構造を確認
                        result.allcount should be >= 0

                      if (result.allcount > 0) {
                        result.novels should not be empty
                        result.novels should have size 1

                        val novel = result.novels.head
                        novel.title should not be empty
                        novel.ncode should not be empty
                        novel.writer should not be empty
                        novel.biggenre should be >= 0
                        novel.genre should be >= 0
                      } else {
                        result.novels should be(empty)
                      }
                    }
      } yield succeed
    }.runToFuture

    "searchByWordメソッドが動作すること" in {
      // 一般的なキーワードで検索（1件のみ）
      for {
        client <- this.client
        _      <- client
                    .searchByWord("魔法", limit = 1)
                    .map { result =>
                      result.allcount should be >= 0

                      if (result.allcount > 0) {
                        result.novels should not be empty
                        result.novels should have size 1

                        val novel = result.novels.head
                        novel.title should not be empty
                        novel.ncode should not be empty
                      } else {
                        result.novels should be(empty)
                      }
                    }
      } yield succeed
    }.runToFuture

    "存在しないキーワードで空の結果を取得できること" in {
      // 存在しないであろう特殊なキーワード
      val params = SearchParams(
        word = Some("存在しないであろう特殊なキーワード99999xyz"),
        lim = Some(1)
      )

      for {
        client <- this.client
        _      <- client
                    .search(params)
                    .map { result =>
                      // 空の結果でも正常なレスポンスが返ること
                      result.allcount should be(0)
                      result.novels should be(empty)
                    }
      } yield succeed
    }.runToFuture

    "getByNcodeメソッドが動作すること" in {
      // まず検索してNコードを取得

      for {
        client <- this.client
        _      <- client
                    .searchByWord("小説", limit = 1)
                    .flatMap { searchResult =>
                      if (searchResult.allcount > 0) {
                        val ncode = searchResult.novels.head.ncode

                        // そのNコードで詳細取得
                        client
                          .getByNcode(ncode)
                          .map { result =>
                            result.allcount should be(1)
                            result.novels should have size 1
                            result.novels.head.ncode should be(ncode)
                          }
                      } else {
                        // 検索結果がない場合はスキップ
                        Task.now(succeed)
                      }
                    }
      } yield succeed

    }.runToFuture

    "複数の検索パラメータを組み合わせたテスト" in {
      val params = SearchParams(
        biggenre = Some(1),   // ハイファンタジー
        lim = Some(1),
        order = Some("hyoka") // 評価順
      )

      for {
        client <- this.client
        _      <- client
                    .search(params)
                    .map { result =>
                      result.allcount should be >= 0

                      if (result.allcount > 0) {
                        result.novels should not be empty
                        val novel = result.novels.head
                        novel.biggenre should be(1) // ハイファンタジーであることを確認
                      }
                      succeed
                    }
      } yield succeed
    }.runToFuture
  }
}
