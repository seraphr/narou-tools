package jp.seraphr.narou.webui.component

import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.narou.webui.{ StoreProvider, StoreWrapper }
import jp.seraphr.narou.webui.action.Actions

import japgolly.scalajs.react.{ React, ScalaComponent, ScalaFnComponent }
import japgolly.scalajs.react.vdom.html_<^._
import typings.antd.antdStrings
import typings.antd.components.Button
import typings.antd.esDrawerDrawerMod.DrawerProps

object NovelDrawer {
  case class Props(novel: Option[NarouNovel], actions: Actions)

  private val detailComponent = ScalaComponent
    .builder[NarouNovel]
    .initialState(false)
    .noBackend
    .renderP { (tScope, tNovel) =>
      val showStory = tScope.state
      val tUrl      = s"https://ncode.syosetu.com/${tNovel.ncode.toLowerCase}/"

      React.Fragment(
        <.div(tNovel.title),
        <.div(f"${tNovel.length}%,3d 文字"),
        <.div(s"${tNovel.genre.text}"),
        <.div(tNovel.keywords.mkString("[", ", ", "]")),
        <.div(<.a(^.href := tUrl)(tUrl)),
        <.div(Button.`type`(antdStrings.link).onClick(_ => tScope.modState(v => !v)).size(antdStrings.small)("あらすじ")),
        AntdDrawer(
          DrawerProps().setOpen(showStory).setSize(400).setClosable(true).setOnClose(_ => tScope.setState(false))
        )(
          <.pre(^.whiteSpace.preWrap)(tNovel.story)
        )
      )
    }
    .build

  val innerComponent = ScalaFnComponent[Props] { case Props(tNovel, tActions) =>
    AntdDrawer(
      DrawerProps().setOpen(tNovel.nonEmpty).setSize(400).setClosable(true).setOnClose(_ => tActions.deselectNovel())
    )(
      tNovel.fold(EmptyVdom)(detailComponent(_))
    )
  }

  private val mStoreWrapper = StoreWrapper.wrapCompletely(StoreProvider.context) { tState =>
    Props(tState.state.selected.novel, tState.actions)
  }

  val component = mStoreWrapper.wrapFn(innerComponent)
  def apply()   = component()
}
