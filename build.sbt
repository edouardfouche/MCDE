name := "MCDE"
organization:= "io.github.edouardfouche"

version := "0.1.0"

scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.11.8", "2.12.8") // prefix with "+" to perform for both .e.g, "+ compile"
//scalacOptions += "-deprecation"

fork in run := true
scalacOptions += "-feature"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "io.github.edouardfouche" %% "datagenerator" % "0.1.0"

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

libraryDependencies += "org.jzy3d" % "jzy3d-api" % "1.0.0" //from "http://maven.jzy3d.org/releases/"
resolvers += "Jzy3d Maven Release Repository" at "http://maven.jzy3d.org/releases"
 
libraryDependencies ++= Seq(
  "org.scalanlp" %% "breeze" % "0.13.1",
  "org.scalanlp" %% "breeze-natives" % "0.13.1"
)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"
// Note: from logback 1.1.5, threads do not inherit the MDC anymore

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
test in assembly := {}

assemblyMergeStrategy in assembly ~= { old =>
{
  case PathList("META-INF", "datagenerator", xs @ _*) => MergeStrategy.first
  case x if x.startsWith("Main") => MergeStrategy.first 
  case x => old(x)
}
}

javacOptions ++= Seq("-encoding", "UTF-8")

////// Sonatype

useGpg := true
pgpReadOnly := false

ThisBuild / organization := "io.github.edouardfouche.MCDE"
ThisBuild / organizationName := "edouardfouche"
ThisBuild / organizationHomepage := Some(url("https://github.com/edouardfouche"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/edouardfouche/MCDE"),
    "scm:git@github.com:edouardfouche/MCDE.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "edouardfouche",
    name  = "Edouard Fouché",
    email = "edouard.fouche@kit.edu",
    url   = url("https://github.com/edouardfouche")
  )
)

ThisBuild / description := "Monte Carlo Dependency Estimation (MCDE)"
ThisBuild / licenses := Seq("AGPLv3" -> url("https://www.gnu.org/licenses/agpl-3.0.en.html"))
ThisBuild / homepage := Some(url("https://github.com/edouardfouche/MCDE"))

ThisBuild /pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild /publishMavenStyle := true

publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

//publishArtifact in Test := false