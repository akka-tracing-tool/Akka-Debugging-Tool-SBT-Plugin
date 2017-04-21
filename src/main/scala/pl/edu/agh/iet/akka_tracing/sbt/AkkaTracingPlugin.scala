package pl.edu.agh.iet.akka_tracing.sbt

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object AkkaTracingPlugin extends AutoPlugin {

  val AppVersion = "0.0.4"
  val AspectJVersion = "1.8.10"

  object Imports {
    lazy val configurationFile: SettingKey[String] = SettingKey[String]("configurationFile",
      "Specifies file to read configuration from; file must be reachable from classpath")
    lazy val collector: SettingKey[Option[ModuleID]] = SettingKey[Option[ModuleID]]("collector",
      "Specifies collector package to be used. Defaults to None.")
    lazy val initDatabaseTask: TaskKey[Unit] = TaskKey[Unit]("initDatabase", "Initializes the database")
    lazy val cleanDatabaseTask: TaskKey[Unit] = TaskKey[Unit]("cleanDatabase", "Cleans the database")
  }

  import Imports._

  override def requires: Plugins = JvmPlugin

  override lazy val projectSettings = Seq(
    configurationFile in Compile := "akka_tracing.conf",
    collector := None,
    sourceGenerators in Compile += Def.task({
      val logger = streams.value.log
      val configFile = (resourceDirectory in Compile).value / (configurationFile in Compile).value
      logger.info("Generating aspect...")
      val files = FilesGenerator.generateAspect(
        configFile, (sourceManaged in Compile).value, (configurationFile in Compile).value
      )
      logger.info("Aspect generated.")
      files
    }).taskValue,
    resourceGenerators in Compile += Def.task({
      val logger = streams.value.log
      val configFile = (resourceDirectory in Compile).value / (configurationFile in Compile).value
      logger.info("Generating aspect weaving configuration...")
      val files = FilesGenerator.generateResource(
        configFile, (resourceManaged in Compile).value
      )
      logger.info("Aspect weaving configuration generated.")
      files
    }).taskValue,
    fork := true,
    javaOptions += s"-javaagent:${findAspectjWeaver.value.get}",
    initDatabaseTask := {
      val logger = streams.value.log
      val classpath = (fullClasspath in Runtime).value
      val config = (configurationFile in Compile).value
      logger.info("Initializing database...")
      DatabaseTasks.initDatabase(classpath.toList.map(_.data), logger, config)
      logger.info("Database initialized.")
    },
    cleanDatabaseTask := {
      val logger = streams.value.log
      val classpath = (fullClasspath in Runtime).value
      val config = (configurationFile in Compile).value
      logger.info("Cleaning database...")
      DatabaseTasks.cleanDatabase(classpath.toList.map(_.data), logger, config)
      logger.info("Database cleaned.")
    },
    libraryDependencies ++= Seq(
      "org.aspectj" % "aspectjweaver" % AspectJVersion,
      "org.aspectj" % "aspectjrt" % AspectJVersion,
      "pl.edu.agh.iet" %% "akka-tracing-core" % AppVersion
    ),
    libraryDependencies ++= {
      collector.value.map(Seq(_))
        .getOrElse({
          val configFile = (resourceDirectory in Compile).value / (configurationFile in Compile).value
          new ConfigurationReader(configFile)
            .getCollectorModule(AppVersion)
            .map(Seq(_))
            .getOrElse(Seq())
        })
    },
    resolvers += Resolver.url("Akka Tracing", url("https://dl.bintray.com/salceson/maven/"))(Resolver.ivyStylePatterns)
  )

  private def findAspectjWeaver: Def.Initialize[Task[Option[File]]] = update map { report =>
    report.matching(moduleFilter(organization = "org.aspectj", name = "aspectjweaver")).headOption
  }
}
