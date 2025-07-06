package jp.seraphr.narou.api

import jp.seraphr.narou.api.model.SearchParams

import monix.execution.Scheduler.Implicits.global
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class NarouApiClientTest extends AsyncFreeSpec with Matchers {

  "NarouApiClient (JS)" - {
    "SearchParams" - {
      "正常に作成されること" in {
        val params = SearchParams(
          word = Some("ファンタジー"),
          biggenre = Some(1),
          lim = Some(5)
        )

        params.word should contain("ファンタジー")
        params.biggenre should contain(1)
        params.lim should contain(5)

        // JS環境では非同期テストのためにFutureを返す
        scala.concurrent.Future.successful(succeed)
      }

      "デフォルトで空であること" in {
        val params = SearchParams()

        params.word should be(None)
        params.biggenre should be(None)
        params.lim should be(None)

        scala.concurrent.Future.successful(succeed)
      }
    }

    "クライアント作成" - {
      "正常に初期化されること" in {
        NarouApiClientPlatform
          .create()
          .map { client =>
            client should not be null
          }
          .runToFuture
      }
    }
  }
}
