import Dependencies._

organization in ThisBuild := "jp.seraphr"

val commonSettings = Def.settings(
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-feature", "-deprecation", "-unchecked", "-Xlint:_,-missing-interpolator",
    "-Ywarn-dead-code",
    "-Ywarn-unused:patvars",
    "-Xfatal-warnings"
  ),
)

lazy val `narou-libs` = (project in file("narou-libs"))
  .settings(
    libraryDependencies ++= Seq(
      narou4j,
      commonsIO,
      slf4j,
      scalaTest % Test
    )
  )
  .settings(commonSettings)

lazy val `narou-tools` = (project in file("narou-tools"))
  .settings(
    libraryDependencies ++= Seq(
      logback,
      scopt
    ))
  .enablePlugins(PackPlugin)
  .settings(PackPlugin.packSettings)
  .settings(
    packMain := Map("narou" -> "jp.seraphr.narou.commands.narou.NarouCommand"),
    packJvmOpts := Map("narou" -> Seq("-Xmx2g"))
  )
  .settings(commonSettings)
  .dependsOn(`narou-libs`)

lazy val `narou-rank` = (project in file("narou-rank"))
  .settings(commonSettings)
  .dependsOn(`narou-libs`)