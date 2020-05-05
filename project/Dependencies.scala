import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._


object Dependencies {
  val scalatestVersion = "3.1.1"
  val scalatestplusVersion = s"${scalatestVersion}.0"
  val circeVersion = "0.13.0"

  object jvm {
    val scalaTest = "org.scalatest" %% "scalatest" % scalatestVersion
    val narou4j = "net.nashihara" % "narou4j" % "1.2.6"
    val commonsIO = "commons-io" % "commons-io" % "2.5"
    val scopt = "com.github.scopt" %% "scopt" % "3.7.1"
    val slf4j = "org.slf4j" % "slf4j-api" % "1.7.+"
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

    val circe = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras"
    ).map(_ % circeVersion)
  }

  object scalajs {
    val scalajsDom = Def.setting("org.scala-js" %%% "scalajs-dom" % "1.0.0")
    val scalatest = Def.setting("org.scalatest" %%% "scalatest" % scalatestVersion)
    val scalacheck = Def.setting("org.scalatestplus" %%% "scalacheck-1-14" % scalatestplusVersion)
    val reactjs = Def.setting("com.github.japgolly.scalajs-react" %%% "core" % "1.6.0")
    val reactjsExtra = Def.setting("com.github.japgolly.scalajs-react" %%% "extra" % "1.6.0")
    val circe = Def.setting(
      Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser",
        "io.circe" %%% "circe-generic-extras"
      ).map(_ % circeVersion)
    )
  }
}