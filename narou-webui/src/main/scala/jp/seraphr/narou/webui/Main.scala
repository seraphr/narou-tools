package jp.seraphr.narou.webui

import java.net.URI

import org.scalajs.dom
import scala.util.Random

import jp.seraphr.narou.{
  DefaultExtractedNovelLoader,
  DropboxNovelDataReader,
  DummyExtractedNovelLoader,
  DummyNovelLoader,
  ExtractedNovelLoader
}
import jp.seraphr.narou.model.{ Genre, NarouNovel, NovelType, UploadType }
import jp.seraphr.narou.webui.state.AppState

import monix.eval.Task
import typings.dropbox.mod.{ Dropbox, DropboxOptions }
import typings.dropbox.typesDropboxTypesMod.files.{ ListFolderArg, Metadata }

object Main {
  import jp.seraphr.js.ScalaJsConverters._
  import jp.seraphr.narou.eval.TaskUtils._

  import monix.execution.Scheduler.Implicits.global

  private val mCurrentURI = new URI("./narou_novels/")
  println(s"===== currentURI = ${mCurrentURI.toString}")
  private val isLocal     = {
    val tLocation = dom.window.location
    tLocation.hostname == "localhost" || new dom.URLSearchParams(tLocation.search).has("local")
  }

  private lazy val mDropboxClient = {
    // NOTE: ここに clientSecret / refreshTokenを埋め込んでいるのは意図的
    // Dropboxは、Client Credentials Flowを実装していないため、clientId / clientSecretだけでは何もできない
    // この refreshToken で取得可能なアクセストークンは、筆者が認めたサブディレクトリへのreadアクセス以外何もできない
    val tOptions = DropboxOptions()
      .setClientId("4gpor2ahiidljm7")
      .setClientSecret("ylkfh2uk5duzcmi")
      .setRefreshToken("ph7DRDr3uEEAAAAAAAAAAeSmlFTSAGZBQcI5JXCv0Bvvevdba6NQT0UknPxIm0a2")

    new Dropbox(tOptions)
  }

  private val mLoaders: Task[Map[String, ExtractedNovelLoader]] = {
    if (isLocal) {
      Task.pure(Map(("dummy", LocalDummyData.dummyLoader)))
    } else {
      for {
        tChildren         <- mDropboxClient.filesListFolder(ListFolderArg("")).asTask
        tReaders           = tChildren
                               .result
                               .entries
                               .toSeq
                               .map { e =>
                                 val tName = e.asInstanceOf[Metadata].name
                                 tName -> new DropboxNovelDataReader(mDropboxClient, tName)
                               }
        tAvailableReaders <- Task.filter(tReaders)(_._2.exists())
        tLoaders           = tAvailableReaders
                               .map { case (n, r) =>
                                 (n, new DefaultExtractedNovelLoader(r))
                               }
                               .toMap
      } yield tLoaders
    }
  }

//  private val mLoader = if (isLocal) {
//    LocalDummyData.dummyLoader
//  } else {
//    new DefaultExtractedNovelLoader(new AjaxNovelDataReader(mCurrentURI))
//  }

  def main(aArgs: Array[String]): Unit = {
    val tNode = dom.document.getElementById("main")

    mLoaders.foreach { tLoaders =>
      StoreProvider(AppState.emptyState.copy(dirNames = tLoaders.keys.toSeq), tLoaders)(RootView()).renderIntoDOM(tNode)
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
