package jp.seraphr.narou.webui.component

import org.scalajs.dom

import jp.seraphr.narou.model.{ NovelConditionParser, NovelConditionWithSource }

import japgolly.scalajs.react.{ vdom, Callback, ScalaFnComponent }
import typings.antd.components.Input
import typings.react.mod.CSSProperties

object NovelConditionEditor {
  case class Props(
      id: String,
      condition: NovelConditionWithSource,
      onParsed: NovelConditionWithSource => Callback
  )

  val component = ScalaFnComponent
    .withHooks[Props]
    .useState(Option.empty[String])
    .render { (props, error) =>
      import typings.antd.antdStrings

      val tStatus = if (error.value.isDefined) antdStrings.error else antdStrings._empty

      Input()
        .withKey(props.id)
        .style(CSSProperties().setWidth("50%"))
        .id(props.id)
        .defaultValue(props.condition.source)
        .status(tStatus)
        .onChange { value =>
          val tChangedValue = value.target.value

          NovelConditionParser(tChangedValue) match {
            case Left(tError)      => error.setState(Some(tError))
            case Right(tCondition) => error.setState(None) >> props.onParsed(tCondition)
          }
        }
    }

  def apply(
      aId: String,
      aCondition: NovelConditionWithSource,
      aOnParsed: NovelConditionWithSource => Callback
  ) = {
    component(Props(aId, aCondition, aOnParsed))
  }

}
