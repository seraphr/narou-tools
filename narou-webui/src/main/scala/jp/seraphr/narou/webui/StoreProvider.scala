package jp.seraphr.narou.webui

import japgolly.scalajs.react.{ CtorType, React, ScalaComponent }
import jp.seraphr.narou.webui.state.{ AppState, NarouWebAppStore }
import japgolly.scalajs.react.vdom.html_<^._
import jp.seraphr.narou.ExtractedNovelLoader
import jp.seraphr.narou.webui.action.{ Actions, DefaultActions }
import jp.seraphr.util.DefaultStateApi

object StoreProvider {
  private var actions: Actions = null
  val context = React.createContext(NarouWebAppStore.emptyStore)
  val component =
    ScalaComponent.builder[(AppState, ExtractedNovelLoader)]
      .initialStateFromProps(_._1)
      .noBackend
      .renderPC {
        case (scope, (_, loader), children) =>
          if (actions == null) {
            val tStateApi = new DefaultStateApi[AppState](
              () => scope.state,
              f => scope.modState(f).flatMap(_ => scope.mountedPure.state).runNow()
            )
            actions = new DefaultActions(loader, {
              f =>
                import monix.execution.Scheduler.Implicits.global
                f(tStateApi).foreach { _ => }
            })
          }

          StoreProvider.context.provide(NarouWebAppStore(actions, scope.state))(children)
      }.build

  def apply(initialState: AppState, loader: ExtractedNovelLoader)(aChildren: CtorType.ChildArg) = {
    component((initialState, loader))(aChildren)
  }
}
