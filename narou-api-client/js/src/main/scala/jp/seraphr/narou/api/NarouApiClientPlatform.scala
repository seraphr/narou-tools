package jp.seraphr.narou.api

import monix.eval.Task
import sttp.client4.impl.monix.FetchMonixBackend

/** JS用のNarouApiClient実装 */
object NarouApiClientPlatform {

  /** JS用のクライアントを作成する */
  def create(): Task[NarouApiClient] = {
    Task.now(new NarouApiClientImpl(FetchMonixBackend(), gzipDecoder = None))
  }

}
