package pl.edu.agh.iet.akka_tracing.sbt

import com.typesafe.config.Config

import scala.collection.JavaConverters._

object Implicits {

  implicit class RichConfig(val config: Config) {
    def getStringListOr(key: String, or: => List[String]): List[String] = {
      try {
        config.getStringList(key).asScala.toList
      } catch {
        case e: Throwable => or
      }
    }
  }

}
