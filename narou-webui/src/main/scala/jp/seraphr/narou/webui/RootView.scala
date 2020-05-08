package jp.seraphr.narou.webui

import java.net.URI

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import jp.seraphr.narou.{ AjaxNovelDataAccessor, DefaultNovelLoader }
import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta }

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

object RootView {
  @nowarn("cat=unused")
  private val css = CSS

  case class Props(
    message: String
  )

  case class State(dataCount: Int, novelsMeta: Option[NarouNovelsMeta], novels: Seq[NarouNovel])

  def apply(message: String) = component(Props(message))

  //  private val mCurrentURI = new URI(org.scalajs.dom.window.location.href).resolve("./narou_novels")
  private val mCurrentURI = new URI("./narou_novels/")
  println(s"===== currentURI = ${mCurrentURI.toString}")
  private val mLoader = new DefaultNovelLoader(new AjaxNovelDataAccessor(mCurrentURI))
  private def loadNovels(aLimit: Int, setMeta: NarouNovelsMeta => Callback, setNovels: Seq[NarouNovel] => Callback): Callback = {
    import monix.execution.Scheduler.Implicits.global

    Callback.future {
      mLoader.metadata.map(setMeta).runToFuture
    } >> Callback.future {
      mLoader.novels.take(aLimit).toListL.map(setNovels).runToFuture
    }
  }

  val component =
    ScalaComponent.builder[Props]("RootView")
      .initialState(State(dataCount = 100, novelsMeta = None, novels = Seq()))
      .renderPS { case (scope, p, s) =>
        import typings.antd.components._

        val metaState = scope.mountedPure.zoomState(_.novelsMeta)(c => s => s.copy(novelsMeta = c))
        val novelsState = scope.mountedPure.zoomState(_.novels)(c => s => s.copy(novels = c))
        val dataCountState = scope.mountedPure.zoomState(_.dataCount)(c => s => s.copy(dataCount = c))
        <.div(
          <.div(p.message),
          Button(onClick = _ => loadNovels(s.dataCount, v => metaState.setState(Some(v)), novelsState.setState))("load!"),
          s.novelsMeta.map(m => <.div(s"novel count = ${m.novelCount}")).whenDefined,
          InputNumber(
            defaultValue = s.dataCount,
            onChange = { v => dataCountState.setStateOption(v.toOption.map(_.toInt)) }
          )(),
          //          ScatterChartExample(s.dataCount).when(false),
          NovelScatterChart(s.novels)
        )
      }
      .build
}
