sbtPlugin := true

val org = "pl.edu.agh.iet"
val appVersion = "0.0.1-SNAPSHOT"

val UsedScalaVersion = "2.10.5"
val Slf4jVersion = "1.7.12"
val ConfigVersion = "1.3.0"
val AspectJVersion = "1.7.2"
val AkkaVersion = "2.3.9"
val ScalaTestVersion = "2.2.4"
val CommonsCodecVersion = "1.9"

name := "akka-tracing-sbt"

version := appVersion

organization := org

scalaVersion := UsedScalaVersion

resolvers += Resolver.url("Akka Tracing", url("http://dl.bintray.com/salceson/maven"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % ConfigVersion,
  "org.slf4j" % "slf4j-api" % Slf4jVersion,
  "org.slf4j" % "slf4j-simple" % Slf4jVersion,
  "org.scalatest" % "scalatest_2.10" % ScalaTestVersion % "test",
  "commons-codec" % "commons-codec" % CommonsCodecVersion,
  "pl.edu.agh.iet" %% "akka-tracing-core" % "0.0.1-SNAPSHOT"
)

lazy val pluginProject = project in file(".")
