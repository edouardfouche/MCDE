logLevel := Level.Warn

//resolvers += Resolver.url("artifactory",
//  url("http://scalast.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.4")
//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")
//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.2.0") //https://github.com/sbt/sbt-dirty-money
//addSbtPlugin("org.scala-sbt.plugins" % "sbt-onejar" % "0.8")