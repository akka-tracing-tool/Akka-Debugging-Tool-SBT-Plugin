package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import com.typesafe.config.ConfigException.Missing
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import pl.edu.agh.iet.akka_debugging.sbt.ReflectionUtils._
import sbt.IO._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable

object AspectsGenerationTask {

  private[this] val aspectTemplateFile = new File(getClass.getClassLoader.getResource("MethodBang.template").getFile)
  private[this] val logger = LoggerFactory.getLogger(getClass)

  //TODO: Hash configFile
  def generateAspects(sourcesDir: File, resourcesDir: File, configurationFile: String): Unit = {
    println(aspectTemplateFile)
    val configFile = new File(s"${resourcesDir.getAbsolutePath}/$configurationFile")
    val actorsList = getListOfActors(configFile)
    val within = generateWithin(actorsList)
    val output = readLines(aspectTemplateFile).map((line) => {
      if (line.contains("<<<ACTORS>>>")) {
        line.replace("<<<ACTORS>>>", within)
      } else {
        line
      }
    })
    val outputFile = new File(s"$sourcesDir/akka/MethodBang.scala")
    write(outputFile, output.mkString(Newline))
  }

  //TODO: Implementation of "*" and no actors list
  private[this] def getListOfActors(configFile: File): List[String] = {
    val config = ConfigFactory.parseFile(configFile)
    val actors = mutable.MutableList[String]()
    try {
      val packages = config.getStringList("akka_debugging.packages")
      for (p <- packages) {
        try {
          val pConf = config.getConfig(s"akka_debugging.$p")
          //          val packageActors = pConf.getStringListOr("actors", () =>
          //            getClassesFromPackage(akka.actor.Actor.getClass, p).map((`class`) => `class`.getSimpleName))
          val packageActors = pConf.getStringList("actors")
          println(packageActors)
          actors ++= packageActors.asScala.toList.map((actor) => s"$p.$actor")
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

  private[this] def getActors(`package`: String, config: Config): List[String] = {
    val actors = mutable.MutableList[String]()
    try {
      val packageConfig = config.getConfig(s"akka_debugging.${`package`}")

    } catch {
      case e: Missing =>
        actors ++= getClassesFromPackage(akka.actor.Actor.getClass, `package`).map(
          (`class`) => s"${`package`}.${`class`.getSimpleName}")
    }
    actors.toList
  }
}

