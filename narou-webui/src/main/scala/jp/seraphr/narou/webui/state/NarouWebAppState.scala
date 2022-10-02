package jp.seraphr.narou.webui.state

import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta }
import jp.seraphr.narou.webui.action.{ Actions, NopActions }

case class NarouWebAppStore(
    actions: Actions,
    state: AppState
)

object NarouWebAppStore {
  val emptyStore = NarouWebAppStore(NopActions, AppState.emptyState)
}

case class AppState(
    dirNames: Seq[String],
    allMeta: Map[String, NarouNovelsMeta],
    selected: Selected
)

object AppState {
  val emptyState = AppState(Seq.empty, Map.empty, Selected(None, None, Seq.empty, None))
}

case class Selected(
    dir: Option[String],
    meta: Option[String],
    novels: Seq[NarouNovel],
    novel: Option[NarouNovel]
)
