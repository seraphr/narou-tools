resolvers += Resolver.bintrayRepo("oyvindberg", "converter")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.12")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.32")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler-sjs06" % "0.17.0")

addSbtPlugin("org.scalablytyped.converter" % "sbt-converter06" % "1.0.0-beta12")
//addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")