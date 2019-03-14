name := "MCDE"

organization:= "io.github.edouardfouche"

organizationHomepage := Some(url("https://github.com/edouardfouche"))

version := "1.0-SNAPSHOT"
scalaVersion := "2.12.8"

scalacOptions += "-deprecation"

useGpg := true 
pgpReadOnly := false

scmInfo := Some(ScmInfo(url("https://github.com/edouardfouche/MCDE"), "scm:git@github.com:edouardfouche/MCDE.git")) // ADJUST
developers := List(
  Developer(
    id    = "edouardfouche", 
    name  = "Edouard Fouché",
    email = "edouard.fouche@kit.edu",
    url   = url("https://github.com/edouardfouche")
  )
)

description := "Monte Carlo Dependency Estimation (MCDE)"
licenses := Seq("AGPLv3" -> url("https://www.gnu.org/licenses/agpl-3.0.en.html")) 
homepage := Some(url("https://github.com/edouardfouche/MCDE")) 
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
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "io.github.edouardfouche" %% "datagenerator" % "0.1.0"

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

 
libraryDependencies ++= Seq(
  "org.scalanlp" %% "breeze" % "0.13.1",
  "org.scalanlp" %% "breeze-natives" % "0.13.1"
)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" // this is for the logging backend



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


