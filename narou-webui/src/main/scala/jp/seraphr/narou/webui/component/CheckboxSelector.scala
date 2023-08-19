package jp.seraphr.narou.webui.component

import japgolly.scalajs.react.{ Callback, ScalaFnComponent }
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.vdom.html_<^.*
import typings.antd.components.{ Button, Checkbox, Divider, Popover }
import typings.antd.libCheckboxGroupMod.CheckboxOptionType
import typings.antd.libTooltipMod.TooltipPlacement

object CheckboxSelector {
  case class Option[A](name: String, value: A)
  case class OptionGroup[A](name: String, values: Seq[Option[A]])
  case class Props[A](
      title: String,
      options: Seq[OptionGroup[A]],
      defaultSelected: Set[A],
      toOptionValue: A => String,
      onChange: Set[A] => Callback
  )

  def component[A] = ScalaFnComponent
    .withHooks[Props[A]]
    .useStateBy(_.defaultSelected)
    .render { (props, selected) =>
      extension (a: A) {
        private def toOptionValue: String = props.toOptionValue(a)
      }

      val tGroupNameToOptions: Seq[(String, Seq[CheckboxOptionType])] = {
        props
          .options
          .map { case OptionGroup(tName, tOptions) =>
            val tOptionTypes = tOptions.map { case Option(tOptionName, tValue) =>
              CheckboxOptionType(tValue.toOptionValue)
                .setLabel(tOptionName)
                .setOnChange { tEvent =>
                  tEvent.preventDefault()
                  tEvent.stopPropagation()
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

  def apply[A](
      title: String,
      options: Seq[OptionGroup[A]],
      defaultSelected: Set[A],
      toOptionValue: A => String,
      onChange: Set[A] => Callback
  ) = {
    component(Props(title, options, defaultSelected, toOptionValue, onChange))
  }

}
