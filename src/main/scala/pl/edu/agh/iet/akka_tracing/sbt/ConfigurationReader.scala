package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import com.typesafe.config.{ Config, ConfigFactory }
import pl.edu.agh.iet.akka_tracing.utils.ConfigUtils._
import sbt._

class ConfigurationReader(configFile: File) {
  private val config = ConfigFactory.parseFile(configFile)
    .getOrElse[Config]("akka_tracing", ConfigFactory.empty("Akka Tracing"))

  def getCollectorModule(appVersion: String): Option[ModuleID] = {
    val collectorConfig = config.getOrElse[Config]("collector", ConfigFactory.empty("Collector"))

    val PackagePrefix = "pl.edu.agh.iet.akka_tracing.collector"
    val NoOpCollectorNames = Seq(
      "noop", "NoOpCollector", s"$PackagePrefix.NoOpCollector"
    )
    val RelationalDatabaseCollectorNames = Seq(
      "relational", "RelationalDatabaseCollector", s"$PackagePrefix.RelationalDatabaseCollector"
    )

    collectorConfig.getOption[String]("className") flatMap {
      case s if NoOpCollectorNames contains s => None
      case s if RelationalDatabaseCollectorNames contains s => Some(
        "pl.edu.agh.iet" %% "akka-tracing-relational-db-collector" % appVersion
      )
      case _ => None
    }
  }

  def getPackages: List[String] = {
    config.getOrElse[List[String]]("packages", List())
  }
}
