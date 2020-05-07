import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

name := "narou-tools"
ThisBuild / organization := "jp.seraphr"

val commonDependencies = Def.settings(
  libraryDependencies ++= Seq(
    scalajs.scalatest.value % "test",
    scalajs.scalacheck.value % "test"
  )
)

val commonSettings = Def.settings(
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-feature", "-deprecation", "-unchecked", "-Xlint:_,-missing-interpolator",
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
    libraryDependencies ++= scalajs.circe.value
  )
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies ++= Seq(
      jvm.narou4j
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

lazy val `narou-webui` = (project in file("narou-webui"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalablyTypedConverterPlugin)
  .settings(commonSettings)
  .settings(
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      scalajs.reactjs.value
    ) ++ scalajs.circe.value,
    Compile / npmDependencies ++= Seq(
      js.react,
      js.reactDom,
      js.recharts,
      js.antd
    ),
    stFlavour := Flavour.Japgolly
  )
  .dependsOn(modelJS)