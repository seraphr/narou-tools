package jp.seraphr.narou.webui.action

import cats.data.OptionT
import jp.seraphr.narou.ExtractedNovelLoader
import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.narou.webui.state.{ AppState, Selected }
import jp.seraphr.util.StateApi

trait Actions {
  def selectMeta(aId: String): Unit
  def selectNovel(aNovel: NarouNovel): Unit
  def deselectNovel(): Unit
}

class DefaultActions(loader: ExtractedNovelLoader, stateApi: StateApi[AppState]) extends Actions {
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
  override def selectMeta(aId: String): Unit = {
    stateApi.modStateTaskOpt { tState =>
      val tMeta = tState.allMeta.get(aId)
      val tTaskOpt = tMeta.traverse { m =>
        loader.load(m.name).toListL.map(Lens.selectedNovels.replace(_))
      }

      OptionT(tTaskOpt)
    }
  }

  override def selectNovel(aNovel: NarouNovel): Unit = {
    stateApi.modState(Lens.selectedNovel.replace(Some(aNovel)))
  }

  override def deselectNovel(): Unit = {
    stateApi.modState(Lens.selectedNovel.replace(None))
  }
}
