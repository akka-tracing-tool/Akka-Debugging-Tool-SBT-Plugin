package pl.edu.agh.iet.akka_tracing.sbt

import org.slf4j.LoggerFactory
import pl.edu.agh.iet.akka_tracing.sbt.FilesGenerator._
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

  val AspectJVersion = "1.7.2"

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
    initDatabaseTask <<= (configurationParser in Compile) map { configParser =>
      logger.info("Initializing database...")
      val databaseTasks = new DatabaseTasks(configParser)
      databaseTasks.initDatabase()
      logger.info("Database initialized.")
    },
    cleanDatabaseTask <<= (configurationParser in Compile) map { configParser =>
      logger.info("Cleaning database...")
      val databaseTasks = new DatabaseTasks(configParser)
      databaseTasks.cleanDatabase()
      logger.info("Database cleaned.")
    },
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
      "org.aspectj" % "aspectjweaver" % AspectJVersion,
      "org.aspectj" % "aspectjrt" % AspectJVersion,
      "pl.edu.agh.iet" %% "akka-tracing-core" % "0.0.2"
    ),
    resolvers += Resolver.url("Akka Tracing", url("https://dl.bintray.com/salceson/maven/"))(Resolver.ivyStylePatterns),
    compile in Compile <<= (compile in Compile) dependsOn initDatabaseTask
  )

  def findAspectjWeaver: Def.Initialize[Task[Option[File]]] = update map { report =>
    report.matching(moduleFilter(organization = "org.aspectj", name = "aspectjweaver")) headOption
  }

}
