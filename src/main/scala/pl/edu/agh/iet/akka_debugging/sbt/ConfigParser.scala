package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import com.typesafe.config.ConfigException.Missing
import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import sbt.IO.readLines

import scala.collection.JavaConverters._
import scala.collection.mutable

final case class ConfigParser(configFile: File, sources: Seq[File]) {

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
        val pConf = config.getConfig(s"akka_debugging.$p")
        val packageActors = pConf.getStringListOr("actors", TracedActorsFinder.findTracedActors(p, sources))
        actors ++= packageActors.toList.map((actor) => s"$p.$actor")
      } catch {
        case e: Missing =>
          actors ++= TracedActorsFinder.findTracedActors(p, sources).map((actor) => s"$p.$actor")
      }
    }
    actors.toList
  }

  def getPackages: List[String] = {
    try {
      val packages = config.getStringList("akka_debugging.packages")
      packages.asScala.toList
    } catch {
      case e: Missing =>
        logger.error("Cannot read packages from configuration file!", e)
        throw e
    }
  }
}
