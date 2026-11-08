package jp.seraphr.narou.webui.component

import japgolly.scalajs.react.{ Children, CtorType, JsComponent }
import typings.antd.esDrawerDrawerMod.DrawerProps

/** Ant DesignのDrawerをscalajs-reactから利用するためのラッパー。 */
private[component] object AntdDrawer {
  private val mComponent =
    JsComponent[DrawerProps, Children.Varargs, Null](typings.antd.mod.Drawer.^.asInstanceOf[scala.scalajs.js.Object])

  def apply(aProps: DrawerProps)(aChildren: CtorType.ChildArg*): JsComponent.Unmounted[DrawerProps, Null] =
    mComponent(aProps)(aChildren*)

}
