import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

name := "narou-tools"
ThisBuild / organization := "jp.seraphr"
//enablePlugins(WorkbenchPlugin)
//localUrl := ("0.0.0.0", 12345)

val commonDependencies = Def.settings(
  libraryDependencies ++= Seq(
    scalajs.scalatest.value % "test",
    scalajs.scalacheck.value % "test"
  )
)

val commonSettings = Def.settings(
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-feature", "-deprecation", "-unchecked",
    // byname-implicitは https://github.com/scala/bug/issues/12072 の問題の抑止のため削る
    "-Xlint:_,-missing-interpolator,-byname-implicit",
    "-Ywarn-dead-code",
    "-Ywarn-unused:patvars",
    "-Werror"
  ),

  Compile / console / scalacOptions ~= {
    _.filterNot(Set("-Werror"))
  }
) ++ commonDependencies

lazy val `narou-libs-model` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("narou-libs-model"))
  .settings(
    libraryDependencies ++= scalajs.circe.value,
    libraryDependencies ++= Seq(
      scalajs.monixReactive.value
    )
  )
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies ++= Seq(
      jvm.narou4j,
      jvm.scalajsStubs
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      scalajs.scalajsDom.value
    )
  )
lazy val modelJVM = `narou-libs-model`.jvm
lazy val modelJS = `narou-libs-model`.js

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
    ))
  .enablePlugins(PackPlugin)
  .settings(
    packMain := Map("narou" -> "jp.seraphr.narou.commands.narou.NarouCommand"),
    packJvmOpts := Map("narou" -> Seq("-Xmx4g"))
  )
  .settings(commonSettings)
  .dependsOn(
    `narou-libs`,
    modelJVM
  )

lazy val `narou-rank` = (project in file("narou-rank"))
  .settings(commonSettings)
  .dependsOn(`narou-libs`)

// -----------------------

val buildResult = settingKey[File]("")
val build = taskKey[File]("")

lazy val `narou-webui` = (project in file("narou-webui"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalablyTypedConverterPlugin)
  .enablePlugins(GhpagesPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      scalajs.reactjs.value
    ) ++ scalajs.circe.value,
    Compile / npmDependencies ++= Seq(
      js.react,
      js.reactDom,
      js.reactDomType,
      js.recharts,
      js.antd
    ),
    stFlavour := Flavour.Japgolly,
    stIgnore ++= List(
      // https://github.com/ScalablyTyped/Converter/issues/324
      "recharts/types/util/CartesianUtils"
    ),
    // css-load設定 fileとかurlは要らんが、scalablytypedデモプロジェクトからそのまま持ってきた
    webpackConfigFile := Some(baseDirectory.value / "custom-scalajs.webpack.config"),
    Compile / npmDevDependencies ++= Seq(
      js.`css-loader`,
      js.`style-loader`,
      js.`file-loader`,
      js.`url-loader`
    ),
    git.remoteRepo := "git@github.com:seraphr/narou-tools.git",
    buildResult := target.value / "scalajs-generated",
    siteSourceDirectory := buildResult.value,
    makeSite := makeSite.dependsOn(build).value,
    build := {
      val tTargetDir = buildResult.value
      val tHtmlFile = baseDirectory.value / "index-fastopt.html"
      val tTargetHtmlFile = tTargetDir / "index.html"
      sbt.IO.delete(tTargetDir)
      val tJsDir = tTargetDir / "js"
      val tWebpackFiles = (Compile / fastOptJS / webpack).value
      tJsDir.mkdirs()
      val tMapping = tWebpackFiles.map(f => f.data -> tJsDir / f.data.name)
      sbt.IO.copy(tMapping)

      sbt.IO.copyFile(tHtmlFile, tTargetDir / "index.html")
      tTargetDir
    },
    ghpagesCleanSite / excludeFilter := { (f: File) =>
      f.isDirectory && f.getName == "narou_novels"
    }
  )
  .dependsOn(modelJS)
