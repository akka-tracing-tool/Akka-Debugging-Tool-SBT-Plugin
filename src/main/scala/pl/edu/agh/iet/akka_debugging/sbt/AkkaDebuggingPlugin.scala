package pl.edu.agh.iet.akka_debugging.sbt

import org.slf4j.LoggerFactory
import pl.edu.agh.iet.akka_debugging.sbt.AspectsGenerationTask._
import pl.edu.agh.iet.akka_debugging.sbt.TracedActorsFinder._
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

  override lazy val projectSettings = inConfig(Compile)(
    Seq(
      aspectsConfigurationFile := "akka_debugging.conf",
      generateAspectsTask := {
        generateAspects(sourceDirectory.value, resourceDirectory.value,
          aspectsConfigurationFile.value, sources.value)
        logger.info("Aspects generated successfully")
      },
      sourceGenerators <+= sourceDirectory map { dir =>
        Seq(dir / "scala" / "akka" / "MethodBang.scala")
      }
    )
  )

}
