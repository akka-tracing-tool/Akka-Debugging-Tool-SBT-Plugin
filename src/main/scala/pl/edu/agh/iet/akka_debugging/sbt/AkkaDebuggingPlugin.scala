package pl.edu.agh.iet.akka_debugging.sbt

import org.slf4j.LoggerFactory
import pl.edu.agh.iet.akka_debugging.sbt.FilesGenerator._
import sbt.Keys._
import sbt._

object AkkaDebuggingPlugin extends AutoPlugin {

  object Settings {
    lazy val generateAspectsTask = TaskKey[Unit]("generateAspects",
      "Reads configuration from file and generates aspects")
    lazy val aspectsConfigurationFile = SettingKey[String]("aspectsConfigurationFile",
      "Specifies file to read configuration from; file must be reachable from classpath")
  }

  import Settings._

  val logger = LoggerFactory.getLogger(getClass)
  var aspectFiles: Seq[File] = Seq()
  var resourceFiles: Seq[File] = Seq()

  override lazy val projectSettings = inConfig(Compile)(
    Seq(
      aspectsConfigurationFile := "akka_debugging.conf",
      generateAspectsTask := {
        val parser = new ConfigParser(resourceDirectory.value / aspectsConfigurationFile.value,
          sources.value)
        aspectFiles = generateAspect(parser, sourceDirectory.value)
        resourceFiles = generateResource(parser, resourceDirectory.value)
      },
      sourceGenerators <+= sourceDirectory map { _ => aspectFiles },
      resourceGenerators <+= resourceDirectory map { _ => resourceFiles }
    )
  )

}
