import org.scalajs.sbtplugin.ScalaJSPlugin._

import sbt._

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val reactVersion         = "17.0.2"
  val scalatestVersion     = "3.2.19"
  val scalatestplusVersion = s"${scalatestVersion}.0"
  val circeVersion         = "0.14.14"
  val monixVersion         = "3.4.1"
  val monocleVersion       = "3.3.0"
  val sttpVersion          = "4.0.10"

  object jvm {
    val scalaTest     = "org.scalatest"    %% "scalatest"        % scalatestVersion
    val commonsIO     = "commons-io"        % "commons-io"       % "2.20.0"
    val scopt         = "com.github.scopt" %% "scopt"            % "4.1.0"
    val slf4j         = "org.slf4j"         % "slf4j-api"        % "1.7.+"
    val logback       = "ch.qos.logback"    % "logback-classic"  % "1.5.18"
    val monix         = "io.monix"         %% "monix"            % monixVersion
    val monixReactive = "io.monix"         %% "monix-reactive"   % monixVersion
    val dropbox       = "com.dropbox.core"  % "dropbox-core-sdk" % "7.0.0"
    val jsoup         = "org.jsoup"         % "jsoup"            % "1.21.2"

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
    val scalacheck    = Def.setting("org.scalatestplus" %%% "scalacheck-1-18" % scalatestplusVersion)
    val reactjs       = Def.setting("com.github.japgolly.scalajs-react" %%% "core" % "2.1.2")
    val reactjsExtra  = Def.setting("com.github.japgolly.scalajs-react" %%% "extra" % "2.1.2")
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
    val react                          = "react"                        -> reactVersion
    val reactDom                       = "react-dom"                    -> reactVersion
    val reactDomType                   = "@types/react-dom"             -> "17.0.11"
    val recharts                       = "recharts"                     -> "2.5.0"
    val antd                           = "antd"                         -> "4.24.8"
    val dropbox                        = "dropbox"                      -> "10.34.0"
    val `node-polyfill-webpack-plugin` = "node-polyfill-webpack-plugin" -> "2.0.1"

    val `css-loader`   = "css-loader"   -> "6.7.3"
    val `style-loader` = "style-loader" -> "3.3.2"
    val `file-loader`  = "file-loader"  -> "6.2.0"
    val `url-loader`   = "url-loader"   -> "4.1.1"

    val webpack          = "5.76.2"
    val webpackDevServer = "3.11.3"
    val typescript       = "5.0.2"
  }
}
