package jp.seraphr.narou.webui

import japgolly.scalajs.react.raw
import org.scalajs.dom

object Main {
  def main(aArgs: Array[String]): Unit = {
    println("call main!")
    val tNode = dom.document.getElementById("main")
    val tCollections = dom.document.getElementsByTagName("div")
    (0 until tCollections.length).foreach(i => println("id: " + tCollections(i).id))
    println(s"node = ${tNode}")
    RootView("hello scalajs-react").renderIntoDOM(tNode)
    raw.React
  }
}
