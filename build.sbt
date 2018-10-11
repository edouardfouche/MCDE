name := "MCDE"

organization:= "com.edouardfouche"

version := "1.0"
scalaVersion := "2.11.8"
//scalaVersion := "2.12.3"
fork in run := true

//scalacOptions += "-Yresolve-term-conflict:package"

//unmanagedJars in Compile += file("lib/uds.jar")
// I don't know whether two lines are required
unmanagedJars in Compile += file("lib/uds.jar")
unmanagedJars in Compile += file("lib/elki-bundle-0.7.2-SNAPSHOT.jar")

//libraryDependencies += "org.scalariform" %% "scalariform" % "0.2.6"
//libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
//libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.14"

// use pprint.pprintln(whatever)
// libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.0"

// libraryDependencies += "org.jzy3d" % "jzy3d-api" % "1.0.0"

libraryDependencies ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "0.13.1",

  // Native libraries are not included by default. add this if you want them (as of 0.7)
  // Native libraries greatly improve performance, but increase jar sizes.
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  "org.scalanlp" %% "breeze-natives" % "0.13.1"

  // The visualization library is distributed separately as well.
  // It depends on LGPL code
  // "org.scalanlp" %% "breeze-viz" % "0.13.1"
)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"
//libraryDependencies += "org.scala-graph" %% "graph-core" % "1.12.0"
//libraryDependencies += "org.scala-graph" %% "graph-dot" % "1.11.5"

// resolvers += "Jzy3d Maven Release Repository" at "http://maven.jzy3d.org/releases"
// resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
// resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"

//sbt-assembly

//import AssemblyKeys._

//assemblySettings

//import com.github.retronym.SbtOneJar._
//oneJarSettings


assemblySettings

import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._

jarName in assembly := s"${name.value}-${version.value}.jar"
test in assembly := {}

//mergeStrategy in assembly := {
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//  case x => MergeStrategy.first
//}

// This was a bad idea, as it led to some portion of ELKI not being included somehow
//mergeStrategy in assembly := {
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//  case x => MergeStrategy.first
//}

javacOptions ++= Seq("-encoding", "UTF-8")

logLevel := Level.Debug
/*
assemblyJarName in assembly := "MCDE.jar"
test in assembly := {}
mainClass in assembly := Some("com.edouardfouche.Main")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

javacOptions ++= Seq("-encoding", "UTF-8")

logLevel := Level.Debug
*/