sbtPlugin := true

val org = "pl.edu.agh.iet"
val appVersion = "0.0.1-SNAPSHOT"

val UsedScalaVersion = "2.10.5"
val Slf4jVersion = "1.7.12"
val ConfigVersion = "1.3.0"
val AspectJVersion = "1.7.2"

name := "akka-debugging-sbt"

version := appVersion

organization := org

scalaVersion := UsedScalaVersion

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % ConfigVersion,
  "org.slf4j" % "slf4j-api" % Slf4jVersion,
  "org.slf4j" % "log4j-over-slf4j" % Slf4jVersion % "test",
  "org.aspectj" % "aspectjweaver" % AspectJVersion,
  "org.aspectj" % "aspectjrt" % AspectJVersion
)

lazy val pluginProject = project in file(".")
