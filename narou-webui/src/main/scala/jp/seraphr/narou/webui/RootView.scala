package jp.seraphr.narou.webui

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExportAll, JSImport }

import jp.seraphr.narou.model.{
  NarouNovel,
  NarouNovelsMeta,
  NovelCondition,
  NovelConditionParser,
  NovelConditionWithSource
}
import jp.seraphr.narou.webui.ScatterData.{ RangeFilter, RepresentativeData }
import jp.seraphr.narou.webui.action.Actions
import jp.seraphr.narou.webui.component.{ NovelDrawer, NovelScatterChartPanel }
import jp.seraphr.narou.webui.component.NovelScatterChartPanel.ScatterDataGroup
import jp.seraphr.narou.webui.state.LazyLoad

import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

object RootView {
  // unusedは scala 3.3で復活する
  //  @nowarn("cat=unused")
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

  private val scatterCandidates = Seq(
    ScatterDataGroup("サンプリング", samplingScatters),
    ScatterDataGroup("代表値", representativeScatters)
  )

  extension [L, R](e: Either[L, R]) {
    def value: R = e match {
      case Right(r) => r
      case Left(l)  => throw new RuntimeException(s"Left($l)")
    }

  }

  private val builtInFilters = Seq(
    NovelConditionParser("all").value,
    NovelConditionParser("length >= 1000000").value,
    NovelConditionParser("bookmark <= 10000").value
  )

  private val innerComponent =
    ScalaComponent
      .builder[Props]("RootView")
      .stateless
      .noBackend
      .render_P { case Props(actions, dirNames, _, allMeta, novels, selectedNovel) =>
        import typings.antd.components._
        import typings.antd.components.Select.Option

        val tSelectDirOptions = dirNames
          .getOrElse(Seq.empty)
          .sorted
          .reverse
          .map { tName =>
            Option.value(tName)(tName).build
          }

        val tScatterChartPanel = NovelScatterChartPanel(
          scatterCandidates,
          AxisData.all,
          builtInFilters,
          defaultState = Some(
            NovelScatterChartPanel.State(
              builtInFilters.head,
              AxisData.bookmark,
              AxisData.evaluationPerBookmark,
              representativeScatters.toSet
            )
          )
        )

        val tSelectMetaOptions = allMeta
          .getOrElse(Map.empty)
          .toSeq
          .sortBy(_._2.novelCount)
          .map { case (tId, tMeta) =>
            Option.value(tId)(s"${tMeta.name}(${tMeta.novelCount})").build
          }

        <.div(
          Select()
            .dropdownMatchSelectWidth(false)
            .loading(dirNames.isLoading)
            .onSelect((tValue, _) => actions.selectDir(tValue.toString))(
              tSelectDirOptions: _*
            ),
          Select()
            .dropdownMatchSelectWidth(false)
            .loading(allMeta.isLoading)
            .onSelect((tValue, _) => actions.selectMeta(tValue.toString))(
              tSelectMetaOptions: _*
            ),
          <.div(s"loaded novel count = ${novels.size}"),
          tScatterChartPanel,
          NovelDrawer()
        )
      }
      .build

  val storeWrapper = StoreWrapper.wrapCompletely(StoreProvider.context)(s =>
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
