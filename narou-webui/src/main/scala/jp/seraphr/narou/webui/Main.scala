package jp.seraphr.narou.webui

import java.net.URI

import org.scalajs.dom
import scala.util.Random

import jp.seraphr.narou.{
  AjaxNovelDataReader,
  DefaultExtractedNovelLoader,
  DummyExtractedNovelLoader,
  DummyNovelLoader
}
import jp.seraphr.narou.model.{ Genre, NarouNovel, NovelType, UploadType }
import jp.seraphr.narou.webui.state.AppState

object Main {
  import monix.execution.Scheduler.Implicits.global

  private val mCurrentURI = new URI("./narou_novels/")
  println(s"===== currentURI = ${mCurrentURI.toString}")
  private val isLocal     = {
    val tLocation = dom.window.location
    tLocation.hostname == "localhost" || new dom.URLSearchParams(tLocation.search).has("local")
  }

  private val mLoader = if (isLocal) {
    LocalDummyData.dummyLoader
  } else {
    new DefaultExtractedNovelLoader(new AjaxNovelDataReader(mCurrentURI))
  }

  def main(aArgs: Array[String]): Unit = {
    val tNode = dom.document.getElementById("main")

    mLoader
      .allMetadata
      .foreach { tMetas =>
        StoreProvider(AppState.emptyState.copy(allMeta = tMetas), mLoader)(RootView()).renderIntoDOM(tNode)
      }
  }

}

object LocalDummyData {
  lazy val dummyLoader = new DummyExtractedNovelLoader(
    Map(
      "dummyDir" -> new DummyNovelLoader(s"dummyNovels${N}", novels)
    )
  )

  private val N      = 1000
  private val rand   = new Random(12)
  private val novels = Vector.tabulate(N) { i =>
    implicit class StrOpt(s: String) {
      def n: String = s + i
    }

    NarouNovel(
      "title".n,
      "ncode".n,
      "userId".n,
      "writer".n,
      "story".n,
      Genre(i, "ジャンル".n),
      "gensaku".n,
      Seq("keyword".n),
      "firstUpload".n,
      "lastUpalod".n,
      NovelType.Serially,
      isFinished = i      % 20 == 0,
      chapterCount = i,
      length = i,
      readTimeMinutes = i,
      isR15 = i           % 10 == 0,
      isBL = (i + 1)      % 20 == 0,
      isGL = (i + 2)      % 20 == 0,
      isZankoku = (i + 3) % 3 == 0,
      isTensei = (i + 4)  % 2 == 0,
      isTenni = (i + 5)   % 3 == 0,
      uploadType = UploadType.PC,
      globalPoint = rand.nextInt(10000),
      bookmarkCount = rand.nextInt(10000),
      reviewCount = rand.nextInt(10000),
      evaluationPoint = rand.nextInt(10000),
      evaluationCount = rand.nextInt(1000),
      illustrationCount = i,
      novelUpdatedAt = "novelUpdatedAt".n,
      updatedAt = "updatedAt".n
    )
  }

}
