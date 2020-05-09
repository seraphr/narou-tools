package jp.seraphr.narou.webui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import jp.seraphr.narou.ExtractedNovelLoader
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
    allMeta: Map[String, NarouNovelsMeta],
    loader: ExtractedNovelLoader
  )

  case class State(novels: Seq[NarouNovel])

  def apply(aAllMeta: Map[String, NarouNovelsMeta], aLoader: ExtractedNovelLoader) = component(Props(aAllMeta, aLoader))

  //  private val mCurrentURI = new URI(org.scalajs.dom.window.location.href).resolve("./narou_novels")
  //  private val mCurrentURI = new URI("./narou_novels/")
  //  println(s"===== currentURI = ${mCurrentURI.toString}")
  //  private val mLoader = new DefaultNovelLoader(new AjaxNovelDataAccessor(mCurrentURI), "all")
  //  private def loadNovels(setNovels: Seq[NarouNovel] => Callback): Callback = {
  //    import monix.execution.Scheduler.Implicits.global
  //
  //    Callback.future {
  //      mLoader.metadata.map(setMeta).runToFuture
  //    } >> Callback.future {
  //      mLoader.novels.take(aLimit).toListL.map(setNovels).runToFuture
  //    }
  //  }
  private def loadNovels(p: Props, aTarget: String, setNovels: Seq[NarouNovel] => Callback): Callback = {
    import monix.execution.Scheduler.Implicits.global

    val tMeta = p.allMeta.get(aTarget)
    tMeta.fold(Callback.empty) { tMeta =>
      Callback.future {
        p.loader.load(aTarget).toListL.map(setNovels).runToFuture
      }
    }
  }

  val component =
    ScalaComponent.builder[Props]("RootView")
      .initialState(State(novels = Seq()))
      .renderPS { case (scope, p, s) =>
        import typings.antd.components._

        val novelsState = scope.mountedPure.zoomState(_.novels)(c => s => s.copy(novels = c))

        val tSelectOptions = p.allMeta.map { case (tId, tMeta) =>
          Option(value = tId)(s"${tMeta.name}(${tMeta.novelCount})").vdomElement
        }.toSeq

        <.div(
          Select[String](
            dropdownMatchSelectWidth = false,
            onSelect = (tValue, _) => loadNovels(p, tValue, novelsState.setState)
          )(
            tSelectOptions: _*
          ),
          <.div(s"loaded novel count = ${s.novels.size}"),
          //          ScatterChartExample(s.dataCount).when(false),
          NovelScatterChart(s.novels)
        )
      }
      .build
}
