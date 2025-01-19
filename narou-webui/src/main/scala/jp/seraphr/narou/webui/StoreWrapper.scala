package jp.seraphr.narou.webui

import japgolly.scalajs.react.{ Children, CtorType, ScalaComponent, ScalaFnComponent }
import japgolly.scalajs.react.feature.Context
import japgolly.scalajs.react.internal.Box

class StoreWrapper[State, Props, WrappedProps](aContext: Context[State])(aToProps: (State, WrappedProps) => Props) {
  import japgolly.scalajs.react.vdom.Implicits._

  def wrap[S, B, CT[-p, +u] <: CtorType[p, u]](
      wrapped: ScalaComponent[Props, S, B, CT]
  )(implicit
      s: CtorType.Summoner[Box[WrappedProps], Children.Varargs]
  ): ScalaFnComponent.Component[WrappedProps, s.CT] = {
    ScalaFnComponent.withChildren[WrappedProps] { (tProps, tChildren) =>
      aContext.consume { tState =>
        wrapped.ctor.applyGeneric(aToProps(tState, tProps))(tChildren)
      }
    }
  }

  def wrapFn[S, B, CT[-p, +u] <: CtorType[p, u]](
      wrapped: ScalaFnComponent[Props, CT]
  )(implicit
      s: CtorType.Summoner[Box[WrappedProps], Children.Varargs]
  ): ScalaFnComponent.Component[WrappedProps, s.CT] = {
    ScalaFnComponent.withChildren[WrappedProps] { (tProps, tChildren) =>
      aContext.consume { tState =>
        wrapped.ctor.applyGeneric(aToProps(tState, tProps))(tChildren)
      }
    }
  }

}

object StoreWrapper {

  /** 対象コンポーネントをwrapして、PropsがUnitなコンポーネントを生成するStoreWrapperを作る */
  def wrapCompletely[State, Props](aContext: Context[State])(
      aToProps: State => Props
  ): StoreWrapper[State, Props, Unit] =
    new StoreWrapper(aContext)((s, _) => aToProps(s))

  /** 対象コンポーネントをwrapして、PropsがWrappedPropsなコンポーネントを生成するStoreWrapperを作る */
  def wrapPartial[State, Props, WrappedProps](aContext: Context[State])(
      aToProps: (State, WrappedProps) => Props
  ): StoreWrapper[State, Props, WrappedProps] =
    new StoreWrapper(aContext)(aToProps)

}
