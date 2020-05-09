package jp.seraphr.narou.webui

import java.net.URI

import jp.seraphr.narou.{ AjaxNovelDataAccessor, DefaultExtractedNovelLoader }
import org.scalajs.dom

object Main {
  import monix.execution.Scheduler.Implicits.global

  private val mCurrentURI = new URI("./narou_novels/")
  println(s"===== currentURI = ${mCurrentURI.toString}")
  private val mLoader = new DefaultExtractedNovelLoader(new AjaxNovelDataAccessor(mCurrentURI))

  def main(aArgs: Array[String]): Unit = {
    val tNode = dom.document.getElementById("main")

    mLoader.allMetadata.foreach { tMetas =>
      RootView(tMetas, mLoader).renderIntoDOM(tNode)
    }
  }
}
