package jp.seraphr.narou.webui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import jp.seraphr.narou.model.{ NarouNovel, NovelCondition }
import jp.seraphr.narou.webui.ScatterData.{ RangeFilter, RepresentativeData }
import jp.seraphr.narou.webui.state.NarouWebAppStore

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

object RootView {
  @nowarn("cat=unused")
  private val css = CSS

  type Props = NarouWebAppStore
  case class State(novels: Seq[NarouNovel])

  def apply() = component()

  implicit class CondOps(c: NovelCondition) {
    def withBookmark100 = c and NovelCondition.bookmark100
  }

  private val innerComponent =
    ScalaComponent.builder[Props]("RootView")
      .stateless
      .render_P { case NarouWebAppStore(actions, p) =>
        import typings.antd.components._
        import typings.antd.components.Select.Option

        val tSelectOptions = p.allMeta.toSeq.sortBy(_._2.novelCount).map { case (tId, tMeta) =>
          Option(tId)(s"${tMeta.name}(${tMeta.novelCount})").build
        }

        <.div(
          Select[String]().dropdownMatchSelectWidth(false)
            .onSelect((tValue, _) => Callback(actions.selectMeta(tValue)))(
              tSelectOptions: _*
            ),
          <.div(s"loaded novel count = ${p.selected.novels.size}"),
          //          ScatterChartExample(s.dataCount).when(false),
          <.div("サンプリング"),
          NovelScatterChart(p.selected.novels, Seq(
            ScatterData.filterAndSampling(NovelCondition.finished.withBookmark100, "red", Sampling.targetCount(1000)),
            ScatterData.filterAndSampling(NovelCondition.finished.not.withBookmark100, "green", Sampling.targetCount(1000))
          )),
          <.div("代表値"),
          {
            val tInterval = 100
            val tMinSectionCount = 50
            val tWindow = 300
            NovelScatterChart(p.selected.novels, Seq(
              ScatterData.range(Some(NovelCondition.all), tWindow, RangeFilter.iqrUpperOutlier, "blue"),
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

  val storeWrapper = new StoreWrapper(StoreProvider.context)(identity)
  val component = storeWrapper.wrap(innerComponent)
}
