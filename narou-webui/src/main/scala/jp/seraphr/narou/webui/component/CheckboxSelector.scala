package jp.seraphr.narou.webui.component

import japgolly.scalajs.react.{ Callback, ScalaFnComponent }
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.vdom.html_<^.*
import typings.antd.components.{ Button, Checkbox, Divider, Popover }
import typings.antd.libCheckboxGroupMod.CheckboxOptionType
import typings.antd.libTooltipMod.TooltipPlacement

class CheckboxSelector[A] {
  case class OptionItem(name: String, value: A)
  case class OptionGroup(name: String, values: Seq[OptionItem])
  case class Props(
      title: String,
      options: Seq[OptionGroup],
      defaultSelected: Set[A],
      toOptionValue: A => String,
      onChange: Set[A] => Callback
  )

  val component = ScalaFnComponent
    .withHooks[Props]
    .useStateBy(_.defaultSelected)
    .render { (props, selected) =>
      extension (a: A) {
        private def toOptionValue: String = props.toOptionValue(a)
      }

      val tGroupNameToOptions: Seq[(String, Seq[CheckboxOptionType])] = {
        props
          .options
          .map { case OptionGroup(tName, tOptions) =>
            val tOptionTypes = tOptions.map { case OptionItem(tOptionName, tValue) =>
              CheckboxOptionType(tValue.toOptionValue)
                .setLabel(tOptionName)
                .setOnChange { tEvent =>
                  val tChecked     = tEvent.target.checked_CheckboxChangeEventTarget
                  val tNewSelected =
                    if (tChecked) selected.value + tValue
                    else selected.value - tValue

                  props.onChange(tNewSelected) >> selected.setState(tNewSelected)
                }
            }

            tName -> tOptionTypes
          }
      }

      import scala.scalajs.js.JSConverters._
      val tPopupPanel =
        tGroupNameToOptions.flatMap { case (tGroupName, tOptions) =>
          Seq[VdomNode](
            Divider.plain(true)(tGroupName),
            Checkbox
              .Group
              .options(tOptions.toJSArray)
              .defaultValue(props.defaultSelected.map(_.toOptionValue).toJSArray)
              .build
          )
        }

      Popover
        .trigger("click")
        .content(VdomArray(tPopupPanel: _*).rawNode)
        .placement(TooltipPlacement.bottomLeft)
        .apply(Button(props.title))
    }

  def apply(
      title: String,
      options: Seq[OptionGroup],
      defaultSelected: Set[A],
      toOptionValue: A => String,
      onChange: Set[A] => Callback
  ) = {
    component(Props(title, options, defaultSelected, toOptionValue, onChange))
  }

}
