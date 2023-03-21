import org.scalajs.sbtplugin.ScalaJSPlugin._

import sbt._

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val reactVersion         = "17.0.2"
  val scalatestVersion     = "3.2.14"
  val scalatestplusVersion = s"${scalatestVersion}.0"
  val circeVersion         = "0.14.3"
  val monixVersion         = "3.4.1"
  val monocleVersion       = "3.2.0"

  object jvm {
    val scalaTest     = "org.scalatest"    %% "scalatest"        % scalatestVersion
    val narou4j       = "net.nashihara"     % "narou4j"          % "1.2.6"
    val commonsIO     = "commons-io"        % "commons-io"       % "2.11.0"
    val scopt         = "com.github.scopt" %% "scopt"            % "4.1.0"
    val slf4j         = "org.slf4j"         % "slf4j-api"        % "1.7.+"
    val logback       = "ch.qos.logback"    % "logback-classic"  % "1.4.5"
    val monix         = "io.monix"         %% "monix"            % monixVersion
    val monixReactive = "io.monix"         %% "monix-reactive"   % monixVersion
    val dropbox       = "com.dropbox.core"  % "dropbox-core-sdk" % "5.3.0"

    val circe = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras"
    ).map(_ % circeVersion)

    val scalajsStubs = "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
    val monocle      = Seq(
      "dev.optics" %% "monocle-core",
      "dev.optics" %% "monocle-macro"
    ).map(_ % monocleVersion)

  }

  object scalajs {
    val scalajsDom    = Def.setting("org.scala-js" %%% "scalajs-dom" % "2.3.0")
    val scalatest     = Def.setting("org.scalatest" %%% "scalatest" % scalatestVersion)
    val scalacheck    = Def.setting("org.scalatestplus" %%% "scalacheck-1-16" % scalatestplusVersion)
    val reactjs       = Def.setting("com.github.japgolly.scalajs-react" %%% "core" % "2.1.1")
    val reactjsExtra  = Def.setting("com.github.japgolly.scalajs-react" %%% "extra" % "2.1.1")
    val monixReactive = Def.setting("io.monix" %%% "monix-reactive" % monixVersion)
    val monoids       = Def.setting("org.typelevel" %%% "monoids" % "0.2.0")
    val circe         = Def.setting(
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
    val react                          = "react"                        -> reactVersion
    val reactDom                       = "react-dom"                    -> reactVersion
    val reactDomType                   = "@types/react-dom"             -> "16.9.13"
    val recharts                       = "recharts"                     -> "2.0.9"
    val antd                           = "antd"                         -> "4.16.6"
    val dropbox                        = "dropbox"                      -> "10.32.0"
    val `node-polyfill-webpack-plugin` = "node-polyfill-webpack-plugin" -> "2.0.1"

    val `css-loader`   = "css-loader"   -> "3.4.2"
    val `style-loader` = "style-loader" -> "1.1.3"
    val `file-loader`  = "file-loader"  -> "5.1.0"
    val `url-loader`   = "url-loader"   -> "3.0.0"
  }
}
