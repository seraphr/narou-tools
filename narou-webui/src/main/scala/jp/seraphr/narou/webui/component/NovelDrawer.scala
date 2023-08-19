package jp.seraphr.narou.webui.component

import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.narou.webui.{ StoreProvider, StoreWrapper }
import jp.seraphr.narou.webui.action.Actions

import japgolly.scalajs.react.{ CallbackTo, React, ScalaComponent, ScalaFnComponent }
import japgolly.scalajs.react.vdom.html_<^._
import typings.antd.antdStrings
import typings.antd.components.{ Button, Drawer }

object NovelDrawer {
  case class Props(novel: Option[NarouNovel], actions: Actions)

  private val detailComponent = ScalaComponent
    .builder[NarouNovel]
    .initialState(false)
    .noBackend
    .renderP { (tScope, tNovel) =>
      val showStory = tScope.state
      val tUrl      = s"https://ncode.syosetu.com/${tNovel.ncode}/"

      React.Fragment(
        <.div(tNovel.title),
        <.div(<.a(^.href := tUrl)(tUrl)),
        <.div(Button.`type`(antdStrings.link).onClick(_ => tScope.modState(v => !v)).size(antdStrings.small)("あらすじ")),
        Drawer
          .visible(showStory)
          .width(400)
          .closable(true)
          .onClose(_ => tScope.setState(false))(
            <.pre(^.whiteSpace.preWrap)(tNovel.story)
          )
      )
    }
    .build

  val innerComponent = ScalaFnComponent[Props] { case Props(tNovel, tActions) =>
    Drawer
      .visible(tNovel.nonEmpty)
      .width(400)
      .closable(true)
      .onClose(_ => tActions.deselectNovel())(
        tNovel.fold(EmptyVdom)(detailComponent(_))
      )
  }

  private val mStoreWrapper = StoreWrapper(StoreProvider.context) { tState =>
    Props(tState.state.selected.novel, tState.actions)
  }

  val component = mStoreWrapper.wrapFn(innerComponent)
  def apply()   = component()
}
