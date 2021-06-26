package jp.seraphr.narou.webui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import jp.seraphr.narou.ExtractedNovelLoader
import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta, NovelCondition }
import jp.seraphr.narou.webui.ScatterData.RepresentativeData

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

  private def loadNovels(p: Props, aTarget: String, setNovels: Seq[NarouNovel] => Callback): Callback = {
    import monix.execution.Scheduler.Implicits.global

    val tMeta = p.allMeta.get(aTarget)
    tMeta.fold(Callback.empty) { tMeta =>
      Callback.future {
        p.loader.load(aTarget).toListL.map(setNovels).runToFuture
      }
    }
  }

  implicit class CondOps(c: NovelCondition) {
    def withBookmark100 = c and NovelCondition.bookmark100
  }

  val component =
    ScalaComponent.builder[Props]("RootView")
      .initialState(State(novels = Seq()))
      .renderPS { case (scope, p, s) =>
        import typings.antd.components._
        import typings.antd.components.Select.Option

        val novelsState = scope.mountedPure.zoomState(_.novels)(c => s => s.copy(novels = c))

        val tSelectOptions = p.allMeta.map { case (tId, tMeta) =>
          Option.value(tId)(s"${tMeta.name}(${tMeta.novelCount})").build
        }.toSeq

        <.div(
          Select.dropdownMatchSelectWidth(false)
            .onSelect((tValue, _) => loadNovels(p, tValue, novelsState.setState))(
            tSelectOptions: _*
          ),
          <.div(s"loaded novel count = ${s.novels.size}"),
          //          ScatterChartExample(s.dataCount).when(false),
          <.div("サンプリング"),
          NovelScatterChart(s.novels, Seq(
            ScatterData.filterAndSampling(NovelCondition.finished.withBookmark100, "red", Sampling.targetCount(1000)),
            ScatterData.filterAndSampling(NovelCondition.finished.not.withBookmark100, "green", Sampling.targetCount(1000))
          )),
          <.div("代表値"),
          {
            val tInterval = 100
            val tMinSectionCount = 50
            NovelScatterChart(s.novels, Seq(
              ScatterData.representative(Some(NovelCondition.all), tInterval, tMinSectionCount, RepresentativeData.top, "skyblue"),
              ScatterData.representative(Some(NovelCondition.finished), tInterval, tMinSectionCount, RepresentativeData.average, "red"),
              ScatterData.representative(Some(NovelCondition.finished), tInterval, tMinSectionCount, RepresentativeData.mean, "orange"),
              ScatterData.representative(Some(NovelCondition.finished.not), tInterval, tMinSectionCount, RepresentativeData.average, "green"),
              ScatterData.representative(Some(NovelCondition.finished.not), tInterval, tMinSectionCount, RepresentativeData.mean, "lightgreen"),
            ))
          }
        )
      }
      .build
}
