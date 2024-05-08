import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

name                     := "narou-tools"
ThisBuild / organization := "jp.seraphr"
//enablePlugins(WorkbenchPlugin)
//localUrl := ("0.0.0.0", 12345)

val commonDependencies = Def.settings(
  libraryDependencies ++= Seq(
    scalajs.scalatest.value  % "test",
    scalajs.scalacheck.value % "test"
  )
)

val isCI = Option(System.getenv("CI")).contains("true")

val commonSettings = Def.settings(
  scalacOptions ++= {
    val tBase = Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Wunused:imports,locals,privates,params",
      "-Xmax-inlines",
      "64"
    )
    val tInCI = Seq(
      "-Werror"
    )

    if (isCI) tBase ++ tInCI else tBase
  },
  Compile / console / scalacOptions ~= {
    _.filterNot(Set("-Werror"))
  }
) ++ commonDependencies

lazy val `narou-libs-model` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("narou-libs-model"))
  .jsConfigure(
    _.enablePlugins(ScalaJSBundlerPlugin).enablePlugins(ScalablyTypedConverterPlugin)
  )
  .settings(
    libraryDependencies ++= scalajs.circe.value,
    libraryDependencies ++= Seq(
      scalajs.monixReactive.value,
      scalajs.monoids.value,
      scalajs.fastparse.value
    ) ++ scalajs.monocle.value
  )
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies ++= Seq(
      jvm.narou4j,
      jvm.commonsIO,
      jvm.scalajsStubs,
      jvm.dropbox
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      scalajs.scalajsDom.value
    ),
    Compile / npmDependencies ++= Seq(
      js.dropbox
    ),
    webpack / version   := Dependencies.js.webpack,
    stTypescriptVersion := Dependencies.js.typescript
  )

lazy val modelJVM = `narou-libs-model`.jvm
lazy val modelJS  = `narou-libs-model`.js

lazy val `narou-libs` = (project in file("narou-libs"))
  .settings(
    libraryDependencies ++= Seq(
      jvm.narou4j,
      jvm.commonsIO,
      jvm.slf4j,
      jvm.scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(
    modelJVM
  )

lazy val `narou-tools` = (project in file("narou-tools"))
  .settings(
    libraryDependencies ++= Seq(
      jvm.logback,
      jvm.scopt
    )
  )
  .enablePlugins(PackPlugin)
  .settings(
    packMain    := Map("narou" -> "jp.seraphr.narou.commands.narou.NarouCommand"),
    packJvmOpts := Map("narou" -> Seq("-Xmx4g"))
  )
  .settings(commonSettings)
  .dependsOn(
    `narou-libs`,
    modelJVM
  )

lazy val `narou-rank` = (project in file("narou-rank")).settings(commonSettings).dependsOn(`narou-libs`)

// -----------------------

val buildResult = settingKey[File]("")
val build       = taskKey[File]("")

lazy val `narou-webui` = (project in file("narou-webui"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalablyTypedConverterPlugin)
  .enablePlugins(GhpagesPlugin)
  .enablePlugins(SitePreviewPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer  := true,
    libraryDependencies ++= Seq(
      scalajs.reactjs.value,
      scalajs.reactjsExtra.value
    ) ++ scalajs.circe.value ++ scalajs.monocle.value,
    Compile / npmDependencies ++= Seq(
      js.react,
      js.reactDom,
      js.reactDomType,
      js.recharts,
      js.antd,
      js.dropbox
    ),
    Compile / npmDevDependencies ++= Seq(
      js.`node-polyfill-webpack-plugin`
    ),
    stFlavour                        := Flavour.ScalajsReact,
    stTypescriptVersion              := Dependencies.js.typescript,
    stIgnore ++= List(
      "type-fest" // なんかエラーになるので、とりあえず取り除いておく
    ),
    // css-load設定 fileとかurlは要らんが、scalablytypedデモプロジェクトからそのまま持ってきた
    webpackConfigFile                := Some(baseDirectory.value / "custom-scalajs.webpack.config"),
    webpack / version                := Dependencies.js.webpack,
    startWebpackDevServer / version  := Dependencies.js.webpackDevServer,
    Compile / npmDevDependencies ++= Seq(
      js.`css-loader`,
      js.`style-loader`,
      js.`file-loader`,
      js.`url-loader`
    ),
    git.remoteRepo                   := "git@github.com:seraphr/narou-tools.git",
    buildResult                      := target.value / "scalajs-generated",
    siteSourceDirectory              := buildResult.value,
    makeSite                         := makeSite.dependsOn(build).value,
    build                            := {
      val tTargetDir      = buildResult.value
      val tHtmlFile       = baseDirectory.value / "index-fastopt.html"
      val tTargetHtmlFile = tTargetDir / "index.html"
      sbt.IO.delete(tTargetDir)
      val tJsDir          = tTargetDir / "js"
      val tWebpackFiles   = (Compile / fastOptJS / webpack).value
      tJsDir.mkdirs()
      val tMapping        = tWebpackFiles.map(f => f.data -> tJsDir / f.data.name)
      sbt.IO.copy(tMapping)

      sbt.IO.copyFile(tHtmlFile, tTargetDir / "index.html")
      tTargetDir
    },
    ghpagesCleanSite / excludeFilter := { (f: File) =>
      f.isDirectory && f.getName == "narou_novels"
    }
  )
  .dependsOn(modelJS)
