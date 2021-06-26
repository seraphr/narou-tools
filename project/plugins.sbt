// resolvers += Resolver.bintrayRepo("oyvindberg", "converter")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.12")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.6.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")

addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta33")
// addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.1")


addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")

// sbt-workbenchが古いakkaを使っている + sbt-ghpagesが、sbt-site経由でakkaを入れてくる
// の組み合わせで、動かなくなるので、削る。
//addSbtPlugin(
//  ("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")
//    .excludeAll(ExclusionRule(organization = "com.typesafe.akka")
//  )
//)
