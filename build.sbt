import Dependencies._

name := "narou-tools"
organization in ThisBuild := "jp.seraphr"

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
)

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
  .dependsOn(`narou-libs`)

lazy val `narou-rank` = (project in file("narou-rank"))
  .settings(commonSettings)
  .dependsOn(`narou-libs`)