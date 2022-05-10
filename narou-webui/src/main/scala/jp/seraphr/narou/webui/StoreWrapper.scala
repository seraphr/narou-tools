package jp.seraphr.narou.webui

import japgolly.scalajs.react.{ CtorType, ScalaComponent, ScalaFnComponent }
import japgolly.scalajs.react.feature.Context

class StoreWrapper[State, Props](aContext: Context[State])(aToProps: State => Props) {
  def wrap[S, B, CT[-p, +u] <: CtorType[p, u]](
      wrapped: ScalaComponent[Props, S, B, CT]
  ): ScalaFnComponent.Component[Unit, CtorType.Children] = {
    ScalaFnComponent.justChildren { tChildren =>
      aContext.consume { tState =>
        import japgolly.scalajs.react.vdom.Implicits._
        wrapped.ctor.applyGeneric(aToProps(tState))(tChildren)
      }
    }
  }

  def wrapFn[S, B, CT[-p, +u] <: CtorType[p, u]](
      wrapped: ScalaFnComponent[Props, CT]
  ): ScalaFnComponent.Component[Unit, CtorType.Children] = {
    ScalaFnComponent.justChildren { tChildren =>
      aContext.consume { tState =>
        import japgolly.scalajs.react.vdom.Implicits._
        wrapped.ctor.applyGeneric(aToProps(tState))(tChildren)
      }
    }
  }

}

object StoreWrapper {
  def apply[State, Props](aContext: Context[State])(aToProps: State => Props) =
    new StoreWrapper(aContext)(aToProps)

}
