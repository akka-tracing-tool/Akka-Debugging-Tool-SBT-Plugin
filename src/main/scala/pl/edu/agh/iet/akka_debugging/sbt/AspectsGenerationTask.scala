package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import Implicits._
import ReflectionUtils._
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

object AspectsGenerationTask {

  private[this] val aspectTemplateFile = getClass.getClassLoader.getResource("MethodBang.template").getFile

  //TODO: Real implementation
  def generateAspects(resourcesDir: File, configurationFile: String): Unit = {
    println(aspectTemplateFile)
    val configFile = new File(s"${resourcesDir.getAbsolutePath}/$configurationFile")
    val config = ConfigFactory.parseFile(configFile)
    try {
      val packages = config.getStringList("akka_debugging.packages")
      for (p <- packages) {
        try {
          val pConf = config.getConfig(s"akka_debugging.$p")
          val packageActors = pConf.getStringListOr("actors", () =>
            getClassesFromPackage(akka.actor.Actor.getClass, p).map((`class`) => `class`.getSimpleName))
          println(packageActors)
        } catch {
          case e: Throwable => println(e)
        }
      }
      println(packages)
    } catch {
      case e: Throwable => throw new IllegalArgumentException(s"Couldn't load configuration file $configurationFile", e)
    }
  }

  private[this] def generateWithin(actors: List[String]): String =
    actors.map(actor => s"within($actor)").mkString("(", " || ", ")")
}

