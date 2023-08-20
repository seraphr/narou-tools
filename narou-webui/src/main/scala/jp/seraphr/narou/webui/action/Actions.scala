package jp.seraphr.narou.webui.action

import jp.seraphr.narou.ExtractedNovelLoader
import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.narou.webui.state.{ AppState, LazyLoad, Selected }
import jp.seraphr.util.StateApi

import cats.data.OptionT
import japgolly.scalajs.react.Callback
import monix.eval.Task

trait Actions {
  def selectDir(aDirName: String): Callback
  def selectMeta(aId: String): Callback
  def selectNovel(aNovel: NarouNovel): Callback
  def deselectNovel(): Callback
}

object NopActions extends Actions {
  override def selectDir(aDirName: String): Callback     = ???
  override def selectMeta(aId: String): Callback         = ???
  override def selectNovel(aNovel: NarouNovel): Callback = ???
  override def deselectNovel(): Callback                 = ???
}

class DefaultActions(
    loaders: Map[String, ExtractedNovelLoader],
    runStateApi: (StateApi[AppState] => Task[_]) => Callback
) extends Actions {
  import cats.syntax.all._
  import monocle.Focus

  private object Lens {
    val selected       = Focus[AppState](_.selected)
    val allMeta        = Focus[AppState](_.allMeta)
    val dir            = Focus[Selected](_.dir)
    val novels         = Focus[Selected](_.novels)
    val novel          = Focus[Selected](_.novel)
    val selectedDir    = selected andThen dir
    val selectedNovels = selected andThen novels
    val selectedNovel  = selected andThen novel
  }

  override def selectDir(aDirName: String): Callback = runStateApi { stateApi =>
    for {
      _          <- stateApi.modState(Lens.allMeta.replace(LazyLoad.Loading))
      tAllMeta   <- loaders(aDirName).allMetadata
      tSetDir     = Lens.selectedDir.replace(Some(aDirName))
      tSetAllMeta = Lens.allMeta.replace(LazyLoad.Loaded(tAllMeta))
      _          <- stateApi.modState(tSetDir andThen tSetAllMeta)
    } yield ()
  }

  override def selectMeta(aId: String): Callback = runStateApi { stateApi =>
    stateApi
      .modStateTaskOpt { tState =>
        val tMeta    = tState.allMeta.getOrElse(Map.empty).get(aId)
        val tLoader  = tState.selected.dir.flatMap(loaders.get)
        val tTaskOpt = (tLoader zip tMeta).traverse { case (tLoader, _) =>
          tLoader.load(aId).toListL.map(Lens.selectedNovels.replace(_))
        }

        OptionT(tTaskOpt)
      }
      .value
  }

  override def selectNovel(aNovel: NarouNovel): Callback = runStateApi { stateApi =>
    stateApi.modState(Lens.selectedNovel.replace(Some(aNovel)))
  }

  override def deselectNovel(): Callback = runStateApi { stateApi =>
    stateApi.modState(Lens.selectedNovel.replace(None))
  }

}
