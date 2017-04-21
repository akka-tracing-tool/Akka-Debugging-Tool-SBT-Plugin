sbtPlugin := true

val org = "pl.edu.agh.iet"
val appVersion = "0.0.4"

val UsedScalaVersion = "2.10.6"
val Slf4jVersion = "1.7.24"
val ConfigVersion = "1.3.1"
val AspectJVersion = "1.8.10"
val AkkaVersion = "2.4.17"
val ScalaTestVersion = "3.0.1"
val CommonsCodecVersion = "1.10"

name := "akka-tracing-sbt"

version := appVersion

organization := org

scalaVersion := UsedScalaVersion

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % ConfigVersion,
  "commons-codec" % "commons-codec" % CommonsCodecVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
)

lazy val pluginProject = project in file(".")
