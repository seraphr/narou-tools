package jp.seraphr.narou.webui

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExportAll, JSImport }

import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta, NovelCondition }
import jp.seraphr.narou.webui.ScatterData.{ RangeFilter, RepresentativeData }
import jp.seraphr.narou.webui.action.Actions
import jp.seraphr.narou.webui.component.NovelDrawer
import jp.seraphr.narou.webui.state.LazyLoad

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

object RootView {
  @nowarn("cat=unused")
  private val css = CSS

  @JSExportAll
  case class Props(
      actions: Actions,
      allDirs: LazyLoad[Seq[String]],
      selectedDir: Option[String],
      allMeta: LazyLoad[Map[String, NarouNovelsMeta]],
      selectedNovels: Seq[NarouNovel],
      selectedNovel: Option[NarouNovel]
  )

  def apply() = component()

  implicit class CondOps(c: NovelCondition) {
    def withBookmark100 = c and NovelCondition.bookmark100
  }

  val samplingScatters = Seq(
    ScatterData.filterAndSampling(NovelCondition.finished.withBookmark100, "red", Sampling.targetCount(1000)),
    ScatterData.filterAndSampling(NovelCondition.finished.not.withBookmark100, "green", Sampling.targetCount(1000))
  )

  val representativeScatters = {
    val tInterval        = 100
    val tMinSectionCount = 50
    val tWindow          = 300
    Seq(
      ScatterData.range(Some(NovelCondition.all), tWindow, RangeFilter.iqrUpperOutlier, "blue"),
      ScatterData
        .representative(Some(NovelCondition.all), tInterval, tMinSectionCount, RepresentativeData.top, "skyblue"),
      ScatterData
        .representative(Some(NovelCondition.finished), tInterval, tMinSectionCount, RepresentativeData.average, "red"),
      ScatterData
        .representative(Some(NovelCondition.finished), tInterval, tMinSectionCount, RepresentativeData.mean, "orange"),
      ScatterData.representative(
        Some(NovelCondition.finished.not),
        tInterval,
        tMinSectionCount,
        RepresentativeData.average,
        "green"
      ),
      ScatterData.representative(
        Some(NovelCondition.finished.not),
        tInterval,
        tMinSectionCount,
        RepresentativeData.mean,
        "lightgreen"
      )
    )
  }

  private val innerComponent =
    ScalaComponent
      .builder[Props]("RootView")
      .stateless
      .render_P { case Props(actions, dirNames, _, allMeta, novels, selectedNovel) =>
        import typings.antd.components._
        import typings.antd.components.Select.Option

        val tSelectDirOptions = dirNames
          .getOrElse(Seq.empty)
          .sorted
          .reverse
          .map { tName =>
            Option(tName)(tName).build
          }

        val tSelectMetaOptions = allMeta
          .getOrElse(Map.empty)
          .toSeq
          .sortBy(_._2.novelCount)
          .map { case (tId, tMeta) =>
            Option(tId)(s"${tMeta.name}(${tMeta.novelCount})").build
          }

        <.div(
          Select[String]()
            .dropdownMatchSelectWidth(false)
            .loading(dirNames.isLoading)
            .onSelect((tValue, _) => Callback(actions.selectDir(tValue)))(
              tSelectDirOptions: _*
            ),
          Select[String]()
            .dropdownMatchSelectWidth(false)
            .loading(allMeta.isLoading)
            .onSelect((tValue, _) => Callback(actions.selectMeta(tValue)))(
              tSelectMetaOptions: _*
            ),
          <.div(s"loaded novel count = ${novels.size}"),
          <.div("サンプリング"),
          NovelScatterChart(
            novels,
            selectedNovel,
            samplingScatters,
            actions.selectNovel
          ),
          <.div("代表値"),
          NovelScatterChart(
            novels,
            selectedNovel,
            representativeScatters,
            actions.selectNovel
          ),
          NovelDrawer()
        )
      }
      .build

  val storeWrapper = new StoreWrapper(StoreProvider.context)(s =>
    Props(
      s.actions,
      s.state.dirNames,
      s.state.selected.dir,
      s.state.allMeta,
      s.state.selected.novels,
      s.state.selected.novel
    )
  )

  val component = storeWrapper.wrap(innerComponent)
}
