package jp.seraphr.narou.api

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

import monix.eval.Task
import sttp.client4.httpclient.monix.HttpClientMonixBackend

/** JVM用のNarouApiClient実装 */
object NarouApiClientPlatform {

  private def decompressGzip(data: Array[Byte]): Array[Byte] = {
    new GZIPInputStream(new ByteArrayInputStream(data)).readAllBytes()
  }

  /** JVM用のクライアントを作成する */
  def create(): Task[NarouApiClient] = {
    HttpClientMonixBackend().map(backend => new NarouApiClientImpl(backend, gzipDecoder = Some(decompressGzip)))
  }

}
