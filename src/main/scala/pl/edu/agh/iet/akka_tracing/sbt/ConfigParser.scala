package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import com.typesafe.config.ConfigException.Missing
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import sbt.IO.readLines

import scala.collection.JavaConverters._
import scala.collection.mutable

final case class ConfigParser(configFile: File) {

  import Implicits._

  private[this] val logger = LoggerFactory.getLogger(getClass)
  private[this] val config = ConfigFactory.parseFile(configFile)
  private[this] val configLines = readLines(configFile).mkString
  private[sbt] val hash: String = DigestUtils.sha512Hex(configLines)

  def getActors: List[String] = {
    val packages = getPackages
    val actors = mutable.MutableList[String]()
    for (p <- packages) {
      try {
        val pConf = config.getConfig(s"akka_tracing.$p")
        val packageActors = pConf.getStringListOr("actors", List("*"))
        actors ++= packageActors.toList.map((actor) => s"$p.$actor")
      } catch {
        case e: Missing =>
          actors ++= List(s"$p.*")
      }
    }
    actors.toList
  }

  def getPackages: List[String] = {
    try {
      val packages = config.getStringList("akka_tracing.packages")
      packages.asScala.toList
    } catch {
      case e: Missing =>
        logger.error("Cannot read packages from configuration file!", e)
        throw e
    }
  }

  def getDatabaseConfig: Config = {
    config.getConfig("akka_tracing.remote")
  }
}
