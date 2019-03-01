name := "MCDE"

organization:= "io.github.edouardfouche"

organizationHomepage := Some(url("https://github.com/edouardfouche"))

version := "1.0-SNAPSHOT"
scalaVersion := "2.12.8"

scalacOptions += "-deprecation"

useGpg := true // not working with old sbt
pgpReadOnly := false

scmInfo := Some(ScmInfo(url("https://github.com/edouardfouche/MCDE"), "scm:git@github.com:edouardfouche/MCDE.git")) // ADJUST
developers := List(
  Developer(
    id    = "edouardfouche", // CHECK!!
    name  = "Edouard Fouché",
    email = "edouard.fouche@kit.edu",
    url   = url("https://github.com/edouardfouche")
  )
)

description := "Monte Carlo Dependency Estimation (MCDE)"
licenses := Seq("AGPLv3" -> url("https://www.gnu.org/licenses/agpl-3.0.en.html")) // ADJUST
homepage := Some(url("https://github.com/edouardfouche/MCDE")) // ADJUST
publishMavenStyle := true
pomIncludeRepository := { _ => false }
publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}



fork in run := true
scalacOptions += "-feature"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "de.lmu.ifi.dbs.elki" % "elki" % "0.7.5"
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"
libraryDependencies += "commons-io" % "commons-io" % "2.6"
resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
 
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

//libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

//sbt-assembly

//import AssemblyKeys._

//assemblySettings

//import com.github.retronym.SbtOneJar._
//oneJarSettings

test in assembly := {}

//assemblyMergeStrategy in assembly := {
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//  case x => MergeStrategy.first
//}

/*
assemblySettings

import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._

jarName in assembly := s"${name.value}-${version.value}.jar"
test in assembly := {}

javacOptions ++= Seq("-encoding", "UTF-8")
*/
logLevel := Level.Debug
