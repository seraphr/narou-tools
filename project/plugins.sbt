// resolvers += Resolver.bintrayRepo("oyvindberg", "converter")

addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("org.xerial.sbt" % "sbt-pack"     % "0.14")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.13.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.2.0")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.21.0")

addSbtPlugin("com.github.sbt"   % "sbt-ghpages" % "0.7.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-site"    % "1.4.1")
