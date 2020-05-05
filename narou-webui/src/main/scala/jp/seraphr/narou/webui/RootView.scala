package jp.seraphr.narou.webui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object RootView {
  case class Props(
    message: String
  )

  def apply(message: String): ScalaFnComponent.Unmounted[Props] = component(Props(message))

  val component =
    ScalaFnComponent[Props](props => <.div(props.message))

}
