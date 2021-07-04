package jp.seraphr.narou.webui.state

import jp.seraphr.narou.model.{ NarouNovel, NarouNovelsMeta }
import jp.seraphr.narou.webui.action.Actions

case class NarouWebAppStore(
  actions: Actions,
  state: AppState
)
case class AppState(
  allMeta: Map[String, NarouNovelsMeta],
  selected: Selected
)

case class Selected(
  meta: Option[String],
  novels: Seq[NarouNovel],
  novel: Option[NarouNovel]
)