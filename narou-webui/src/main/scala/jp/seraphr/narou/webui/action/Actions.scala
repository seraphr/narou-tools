package jp.seraphr.narou.webui.action

import cats.data.OptionT
import jp.seraphr.narou.ExtractedNovelLoader
import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.narou.webui.state.{ AppState, Selected }
import jp.seraphr.util.StateApi
import monix.eval.Task

trait Actions {
  def selectMeta(aId: String): Unit
  def selectNovel(aNovel: NarouNovel): Unit
  def deselectNovel(): Unit
}

object NopActions extends Actions {
  override def selectMeta(aId: String): Unit = ???
  override def selectNovel(aNovel: NarouNovel): Unit = ???
  override def deselectNovel(): Unit = ???
}

class DefaultActions(loader: ExtractedNovelLoader, runStateApi: (StateApi[AppState] => Task[_]) => Unit) extends Actions {
  import cats.syntax.all._
  import monocle.Focus

  private object Lens {
    val selected = Focus[AppState](_.selected)
    val allMeta = Focus[AppState](_.allMeta)
    val novels = Focus[Selected](_.novels)
    val novel = Focus[Selected](_.novel)
    val selectedNovels = selected andThen novels
    val selectedNovel = selected andThen novel
  }
  override def selectMeta(aId: String): Unit = runStateApi { stateApi =>
    stateApi.modStateTaskOpt { tState =>
      val tMeta = tState.allMeta.get(aId)
      val tTaskOpt = tMeta.traverse { _ =>
        loader.load(aId).toListL.map(Lens.selectedNovels.replace(_))
      }

      OptionT(tTaskOpt)
    }.value
  }

  override def selectNovel(aNovel: NarouNovel): Unit = runStateApi { stateApi =>
    stateApi.modState(Lens.selectedNovel.replace(Some(aNovel)))
  }

  override def deselectNovel(): Unit = runStateApi { stateApi =>
    stateApi.modState(Lens.selectedNovel.replace(None))
  }
}
