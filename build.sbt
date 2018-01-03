import Dependencies._

organization in ThisBuild := "jp.seraphr"

lazy val `narou-libs` = (project in file("narou-libs"))
  .settings(
    libraryDependencies ++= Seq(
      narou4j,
      commonsIO,
      slf4j,
      scalaTest % Test
    )
  )

lazy val `narou-tools` = (project in file("narou-tools"))
  .settings(
    libraryDependencies ++= Seq(
      logback,
      scopt
    ))
  .dependsOn(`narou-libs`)

lazy val `narou-rank` = (project in file("narou-rank"))
  .settings()
  .dependsOn(`narou-libs`)