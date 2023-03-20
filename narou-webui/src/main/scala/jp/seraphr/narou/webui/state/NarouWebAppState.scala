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

sealed trait LazyLoad[+A] {
  def getOrElse[B >: A](emptyValue: => B): B = this match {
    case LazyLoad.Init | LazyLoad.Loading => emptyValue
    case LazyLoad.Loaded(value)           => value
  }

  lazy val isLoading: Boolean = this == LazyLoad.Loading
}
object LazyLoad           {
  case object Init               extends LazyLoad[Nothing]
  case object Loading            extends LazyLoad[Nothing]
  case class Loaded[A](value: A) extends LazyLoad[A]
}

case class AppState(
    dirNames: LazyLoad[Seq[String]],
    allMeta: LazyLoad[Map[String, NarouNovelsMeta]],
    selected: Selected
)

object AppState {
  val emptyState = AppState(LazyLoad.Init, LazyLoad.Init, Selected(None, None, Seq.empty, None))
}

case class Selected(
    dir: Option[String],
    meta: Option[String],
    novels: Seq[NarouNovel],
    novel: Option[NarouNovel]
)
