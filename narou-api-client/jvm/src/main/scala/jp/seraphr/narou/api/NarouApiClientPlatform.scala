package jp.seraphr.narou.api

import monix.eval.Task
import sttp.client4.httpclient.monix.HttpClientMonixBackend

/** JVM用のNarouApiClient実装 */
object NarouApiClientPlatform {

  /** JVM用のクライアントを作成する */
  def create(): Task[NarouApiClient] = {
    HttpClientMonixBackend().map(backend => new NarouApiClientImpl(backend))
  }

}
