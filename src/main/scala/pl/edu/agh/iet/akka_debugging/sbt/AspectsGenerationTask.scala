package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import sbt.IO._

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source

object AspectsGenerationTask {

  import Implicits._

  private[this] val aspectTemplateFile = new File(getClass.getClassLoader.getResource("MethodBang.template").getFile)
  private[this] val logger = LoggerFactory.getLogger(getClass)

  //TODO: Hash configFile
  def generateAspects(sourcesDir: File, resourcesDir: File,
                      configurationFile: String, sources: Seq[File]): Unit = {
    val configFile = new File(s"${resourcesDir.getAbsolutePath}/$configurationFile")
    val configFileContents = Source.fromFile(configFile).mkString
    val configFileHash = DigestUtils.sha512Hex(configFileContents)
    logger.info(s"The config file hash is $configFileHash")
    val actorsList = getListOfActors(configFile, sources)
    val within = generateWithin(actorsList)
    val output = readLines(aspectTemplateFile).map((line) => {
      if (line.contains("<<<ACTORS>>>")) {
        line.replace("<<<ACTORS>>>", within)
      } else {
        line
      }
    })
    val outputFile = new File(s"$sourcesDir/scala/akka/MethodBang.scala")
    write(outputFile, output.mkString(Newline))
  }

  //TODO: Implementation of no actors list
  private[this] def getListOfActors(configFile: File, sources: Seq[File]): List[String] = {
    val config = ConfigFactory.parseFile(configFile)
    val actors = mutable.MutableList[String]()
    try {
      val packages = config.getStringList("akka_debugging.packages")
      for (p <- packages) {
        try {
          val pConf = config.getConfig(s"akka_debugging.$p")
          val packageActors = pConf.getStringListOr("actors", () =>
            TracedActorsFinder.findTracedActors(p, sources))
          actors ++= packageActors.toList.map((actor) => s"$p.$actor")
        } catch {
          case e: Throwable =>
            logger.error(s"Cannot read actors for package $p", e)
            throw e
        }
      }
      actors.toList
    } catch {
      case e: Throwable =>
        throw new IllegalArgumentException(s"Couldn't load configuration file ${configFile.getName}", e)
    }
  }

  private[this] def generateWithin(actors: List[String]): String =
    actors.map(actor => s"within($actor)").mkString("(", " || ", ")")
}
