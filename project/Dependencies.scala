import org.scalajs.sbtplugin.ScalaJSPlugin._

import sbt._

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val reactVersion         = "19.2.8"
  val scalatestVersion     = "3.2.20"
  val scalatestplusVersion = s"${scalatestVersion}.0"
  val circeVersion         = "0.14.16"
  val monixVersion         = "3.4.1"
  val monocleVersion       = "3.3.0"
  val sttpVersion          = "4.0.26"

  object jvm {
    val scalaTest     = "org.scalatest"    %% "scalatest"        % scalatestVersion
    val commonsIO     = "commons-io"        % "commons-io"       % "2.22.0"
    val scopt         = "com.github.scopt" %% "scopt"            % "4.1.0"
    val slf4j         = "org.slf4j"         % "slf4j-api"        % "1.7.+"
    val logback       = "ch.qos.logback"    % "logback-classic"  % "1.6.3"
    val monix         = "io.monix"         %% "monix"            % monixVersion
    val monixReactive = "io.monix"         %% "monix-reactive"   % monixVersion
    val dropbox       = "com.dropbox.core"  % "dropbox-core-sdk" % "8.0.2"
    val jsoup         = "org.jsoup"         % "jsoup"            % "1.23.1"

    val circe = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)

    val scalajsStubs = "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
    val monocle      = Seq(
      "dev.optics" %% "monocle-core",
      "dev.optics" %% "monocle-macro"
    ).map(_ % monocleVersion)

  }

  object scalajs {
    val scalajsDom    = Def.setting("org.scala-js" %%% "scalajs-dom" % "2.8.1")
    val scalatest     = Def.setting("org.scalatest" %%% "scalatest" % scalatestVersion)
    val scalacheck    = Def.setting("org.scalatestplus" %%% "scalacheck-1-19" % scalatestplusVersion)
    val reactjs       = Def.setting("com.github.japgolly.scalajs-react" %%% "core" % "4.0.0")
    val reactjsExtra  = Def.setting("com.github.japgolly.scalajs-react" %%% "extra" % "4.0.0")
    val monixReactive = Def.setting("io.monix" %%% "monix-reactive" % monixVersion)
    val monoids       = Def.setting("org.typelevel" %%% "monoids" % "0.2.0")
    val circe         = Def.setting(
      Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % circeVersion)
    )

    val monocle = Def.setting(
      Seq(
        "dev.optics" %%% "monocle-core",
        "dev.optics" %%% "monocle-macro"
      ).map(_ % monocleVersion)
    )

    val fastparse = Def.setting("com.lihaoyi" %%% "fastparse" % "3.1.1")
    val sttp      = Def.setting(
      Seq(
        "com.softwaremill.sttp.client4" %%% "core",
        "com.softwaremill.sttp.client4" %%% "circe",
        "com.softwaremill.sttp.client4" %%% "monix"
      ).map(_ % sttpVersion)
    )

  }

  object js {
    val react        = "react"            -> reactVersion
    val reactDom     = "react-dom"        -> reactVersion
    val reactType    = "@types/react"     -> "19.2.18"
    val reactDomType = "@types/react-dom" -> "19.2.4"
    val reactIs      = "react-is"         -> reactVersion
    val recharts     = "recharts"         -> "3.10.1"
    val antd         = "antd"             -> "6.6.1"
    val dropbox      = "dropbox"          -> "10.46.0"

    val `css-loader`   = "css-loader"   -> "7.1.4"
    val `style-loader` = "style-loader" -> "4.0.0"

    val webpack          = "5.109.2"
    val webpackDevServer = "6.0.0"
    val typescript       = "7.0.2"
  }
}
