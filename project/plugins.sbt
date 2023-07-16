// resolvers += Resolver.bintrayRepo("oyvindberg", "converter")

addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("org.xerial.sbt" % "sbt-pack"     % "0.17")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.13.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.0")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.21.1")

addSbtPlugin("com.github.sbt" % "sbt-ghpages" % "0.8.0")
