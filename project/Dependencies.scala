import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val reactVersion = "17.0.2"
  val scalatestVersion = "3.1.1"
  val scalatestplusVersion = s"${scalatestVersion}.0"
  val circeVersion = "0.13.0"
  val monixVersion = "3.2.1"
  val monocleVersion = "3.0.0-RC2"

  object jvm {
    val scalaTest = "org.scalatest" %% "scalatest" % scalatestVersion
    val narou4j = "net.nashihara" % "narou4j" % "1.2.6"
    val commonsIO = "commons-io" % "commons-io" % "2.5"
    val scopt = "com.github.scopt" %% "scopt" % "3.7.1"
    val slf4j = "org.slf4j" % "slf4j-api" % "1.7.+"
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
    val monix = "io.monix" %% "monix" % monixVersion
    val monixReactive = "io.monix" %% "monix-reactive" % monixVersion

    val circe = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras"
    ).map(_ % circeVersion)

    val scalajsStubs = "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided"
    val monocle = Seq(
      "dev.optics" %% "monocle-core",
      "dev.optics" %% "monocle-macro"
    ).map(_ % monocleVersion)
  }

  object scalajs {
    val scalajsDom = Def.setting("org.scala-js" %%% "scalajs-dom" % "1.0.0")
    val scalatest = Def.setting("org.scalatest" %%% "scalatest" % scalatestVersion)
    val scalacheck = Def.setting("org.scalatestplus" %%% "scalacheck-1-14" % scalatestplusVersion)
    val reactjs = Def.setting("com.github.japgolly.scalajs-react" %%% "core" % "1.7.7")
    val reactjsExtra = Def.setting("com.github.japgolly.scalajs-react" %%% "extra" % "1.7.7")
    val monixReactive = Def.setting("io.monix" %%% "monix-reactive" % monixVersion)
    val circe = Def.setting(
      Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser",
        "io.circe" %%% "circe-generic-extras"
      ).map(_ % circeVersion)
    )
    val monocle = Def.setting(
      Seq(
        "dev.optics" %%% "monocle-core",
        "dev.optics" %%% "monocle-macro"
      ).map(_ % monocleVersion)
    )
  }

  object js {
    val react = "react" -> reactVersion
    val reactDom = "react-dom" -> reactVersion
    val reactDomType = "@types/react-dom" -> "16.9.13" 
    val recharts = "recharts" -> "2.0.9"
    val antd = "antd" -> "4.16.6"

    val `css-loader` = "css-loader" -> "3.4.2"
    val `style-loader` = "style-loader" -> "1.1.3"
    val `file-loader` = "file-loader" -> "5.1.0"
    val `url-loader` = "url-loader" -> "3.0.0"
  }
}
