import Dependencies._

organization in ThisBuild := "jp.seraphr"

lazy val `narou-libs` = (project in file("narou-libs"))
  .settings(
    libraryDependencies ++= Seq(
      narou4j,
      commonsIO,
      scalaTest % Test,
    )
  )

lazy val `narou-tools` = (project in file("narou-tools"))
  .settings()
  .dependsOn(`narou-libs`)

