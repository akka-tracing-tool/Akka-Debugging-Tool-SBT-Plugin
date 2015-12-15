package pl.edu.agh.iet.akka_tracing.sbt

import org.slf4j.LoggerFactory
import FilesGenerator._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

import scala.language.postfixOps

object AkkaTracingPlugin extends AutoPlugin {
  val logger = LoggerFactory.getLogger(getClass)

  object Settings {
    private[sbt] lazy val configurationParser = SettingKey[ConfigParser]("configurationParser", "Internal use")
    lazy val aspectsConfigurationFile = SettingKey[String]("aspectsConfigurationFile",
      "Specifies file to read configuration from; file must be reachable from classpath")
    lazy val initDatabaseTask = TaskKey[Unit]("initDatabase", "Inits the database")
    lazy val cleanDatabaseTask = TaskKey[Unit]("cleanDatabase", "Cleans the database")
  }

  import Settings._

  override def requires: Plugins = JvmPlugin

  override lazy val projectSettings = Seq(
    aspectsConfigurationFile in Compile := "akka_tracing.conf",
    configurationParser := new ConfigParser(
      (resourceDirectory in Compile).value / (aspectsConfigurationFile in Compile).value),
    sourceGenerators in Compile += Def.task({
      logger.info("Generating aspects...")
      val files = generateAspect((configurationParser in Compile).value, (sourceManaged in Compile).value,
        (aspectsConfigurationFile in Compile).value)
      logger.info("Aspects generated.")
      files
    }).taskValue,
    resourceGenerators in Compile += Def.task({
      logger.info("Generating aspects weaving configuration...")
      val files = generateResource((configurationParser in Compile).value, (resourceManaged in Compile).value)
      logger.info("Aspects weaving configuration generated.")
      files
    }).taskValue,
    fork := true,
    javaOptions += s"-javaagent:${findAspectjWeaver.value.get}",
    initDatabaseTask := Def.task {
      logger.info("Initializing database...")
      val databaseTasks = new DatabaseTasks(configurationParser.value)
      databaseTasks.initDatabase()
      logger.info("Database initialized.")
    },
    cleanDatabaseTask := Def.task {
      logger.info("Cleaning database...")
      val databaseTasks = new DatabaseTasks(configurationParser.value)
      databaseTasks.cleanDatabase()
      logger.info("Database cleaned.")
    },
    compile in Compile <<= (compile in Compile) dependsOn initDatabaseTask
  )

  def findAspectjWeaver: Def.Initialize[Task[Option[File]]] = update map { report =>
    report.matching(moduleFilter(organization = "org.aspectj", name = "aspectjweaver")) headOption
  }

}
