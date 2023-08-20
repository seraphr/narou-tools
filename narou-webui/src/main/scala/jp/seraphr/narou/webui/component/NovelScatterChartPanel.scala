package jp.seraphr.narou.webui.component

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

import jp.seraphr.narou.model.{ NarouNovel, NovelConditionWithSource }
import jp.seraphr.narou.webui.{ AxisData, ScatterData, StoreProvider, StoreWrapper }
import jp.seraphr.narou.webui.state.NarouWebAppStore

import japgolly.scalajs.react.{ Callback, ScalaFnComponent }
import japgolly.scalajs.react.vdom.html_<^.*
import typings.antd.antdStrings
import typings.antd.components.{ Button, Dropdown, Input }
import typings.antd.libMenuHooksUseItemsMod.MenuItemType
import typings.antd.libMenuMod.MenuProps

object NovelScatterChartPanel {
  case class State(
      filter: NovelConditionWithSource,
      axisX: AxisData,
      axisY: AxisData,
      scatters: Set[ScatterData]
  )
  case class ScatterDataGroup(
      name: String,
      data: Seq[ScatterData]
  )
  case class Props(
      scatterCandidates: Seq[ScatterDataGroup],
      axisCandidates: Seq[AxisData],
      builtInFilters: Seq[NovelConditionWithSource],
      defaultState: Option[State],
      novels: Seq[NarouNovel],
      selectedNovel: Option[NarouNovel],
      onSelectNovel: NarouNovel => Callback
  )

  private val mCounter         = new java.util.concurrent.atomic.AtomicInteger(0)
  private def nextId(): String = s"editingFilterId_${mCounter.incrementAndGet()}"

  private def defaultState(p: Props): State = State(
    filter = p.builtInFilters.head,
    axisX = p.axisCandidates.head,
    axisY = p.axisCandidates(1),
    scatters = p.scatterCandidates.head.data.toSet
  )

  private val ScatterCheckboxSelector = new CheckboxSelector[ScatterData]

  private val innerComponent = ScalaFnComponent
    .withHooks[Props]
    .useStateBy(p => p.defaultState.getOrElse(defaultState(p)))
    .useState(nextId())
    .render { (props, state, editingId) =>
      val tScatterDataOptions: Seq[ScatterCheckboxSelector.OptionGroup] = props
        .scatterCandidates
        .map { tGroup =>
          ScatterCheckboxSelector.OptionGroup(
            tGroup.name,
            tGroup
              .data
              .map { tScatterData =>
                ScatterCheckboxSelector.OptionItem(tScatterData.name, tScatterData)
              }
          )
        }

      val tDataButtons = Input
        .Group
        .compact(true)(
          AxisSelector(
            "X軸",
            props.axisCandidates,
            state.value.axisX,
            tAxis => state.modState(_.copy(axisX = tAxis))
          ),
          AxisSelector(
            "Y軸",
            props.axisCandidates,
            state.value.axisY,
            tAxis => state.modState(_.copy(axisY = tAxis))
          ),
          ScatterCheckboxSelector(
            "データ系列",
            tScatterDataOptions,
            state.value.scatters,
            _.name,
            tScatters => state.modState(_.copy(scatters = tScatters))
          )
        )

      val tFilter = Input
        .Group
        .compact(true)(
          NovelConditionEditor(
            editingId.value,
            state.value.filter,
            tFilter => state.modState(_.copy(filter = tFilter))
          ),
          Dropdown
            .triggerVarargs(antdStrings.click)
            .menu(
              MenuProps().setItems(
                props
                  .builtInFilters
                  .map { tFilter =>
                    MenuItemType(tFilter.name).setLabel(
                      Button
                        .`type`(antdStrings.text_)
                        .onClick(_ => state.modState(_.copy(filter = tFilter)) >> editingId.setState(nextId()))(
                          tFilter.name
                        )
                    )
                  }
                  .toJSArray
              )
            )(Button("built-in filter"))
        )

      val tScatterChart = NovelScatterChart(
        props.novels.filter(state.value.filter.predicate),
        props.selectedNovel,
        state.value.axisX,
        state.value.axisY,
        state.value.scatters.toSeq,
        props.onSelectNovel
      )

      <.div(
        tDataButtons,
        tFilter,
        tScatterChart
      )
    }

  /** StoreWrapperによってWrapされた後のProps */
  case class OuterProps(
      scatterCandidates: Seq[ScatterDataGroup],
      axisCandidates: Seq[AxisData],
      builtInFilters: Seq[NovelConditionWithSource],
      defaultState: Option[State]
  ) {
    private[NovelScatterChartPanel] def toInnerProps(store: NarouWebAppStore) = Props(
      scatterCandidates,
      axisCandidates,
      builtInFilters,
      defaultState,
      store.state.selected.novels,
      store.state.selected.novel,
      store.actions.selectNovel
    )

  }

  private val storeWrapper = StoreWrapper.wrapPartial[NarouWebAppStore, Props, OuterProps](StoreProvider.context) {
    (store, outerProps) => outerProps.toInnerProps(store)
  }

  // NarouWebAppStoreから供給されるものをコンテキストで受け取るようにして、それ以外がPropsになっているコンポーネント
  val component = storeWrapper.wrapFn(innerComponent)

  def apply(
      scatterCandidates: Seq[ScatterDataGroup],
      axisCandidates: Seq[AxisData],
      builtInFilters: Seq[NovelConditionWithSource],
      defaultState: Option[State]
  ) = {
    withProps(OuterProps(scatterCandidates, axisCandidates, builtInFilters, defaultState))
  }

  def withProps(props: OuterProps) = component(props)()
}

object AxisSelector {
  case class Props(
      title: String,
      candidates: Seq[AxisData],
      selected: AxisData,
      onChange: AxisData => Callback
  )

  val component = ScalaFnComponent[Props] { tProps =>
    Dropdown
      .triggerVarargs(antdStrings.click)
      .menu(
        MenuProps()
          .setSelectable(true)
          .setSelectedKeysVarargs(tProps.selected.name)
          .setItems(
            tProps
              .candidates
              .map { tAxis =>
                MenuItemType(tAxis.name).setLabel(
                  Button
                    .`type`(antdStrings.text_)
                    .onClick(_ => tProps.onChange(tAxis))(
                      tAxis.name
                    )
                )
              }
              .toJSArray
          )
      )(Button(tProps.title))
  }

  def apply(
      title: String,
      candidates: Seq[AxisData],
      selected: AxisData,
      onChange: AxisData => Callback
  ) = component(Props(title, candidates, selected, onChange))

}
