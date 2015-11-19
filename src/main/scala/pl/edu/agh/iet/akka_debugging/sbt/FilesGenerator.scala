package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import sbt.IO._

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source

object FilesGenerator {

  import Implicits._

  private[this] val aspectTemplateFile = new File(getClass.getClassLoader.getResource("MethodBang.template").getFile)
  private[this] val aopTemplateFile = new File(getClass.getClassLoader.getResource("aop.template").getFile)
  private[this] val logger = LoggerFactory.getLogger(getClass)

  def generateAspect(configurationParser: ConfigParser, sourcesDir: File): Seq[File] = {
    val hash = configurationParser.hash
    val aspectFile = new File(s"$sourcesDir/scala/akka/MethodBang.scala")

    if (aspectFile.exists()) {
      val regex = s"""(?s).*?Configuration hash: <$hash>.*?""".r
      val aspectContents = Source.fromFile(aspectFile).mkString
      aspectContents match {
        case regex(_ *) =>
          return Seq(aspectFile)
        case _ =>
      }
    }

    val actorsList = configurationParser.getActors
    val within = generateWithin(actorsList)
    val output = readLines(aspectTemplateFile).map((line) => {
      if (line.contains("<<<ACTORS>>>")) {
        line.replace("<<<ACTORS>>>", within)
      } else if (line.contains("<<<HASH>>>")) {
        line.replace("<<<HASH>>>", s"<$hash>")
      } else {
        line
      }
    })
    write(aspectFile, output.mkString(Newline))
    Seq(aspectFile)
  }

  private[this] def generateWithin(actors: List[String]): String =
    actors.map(actor => s"within($actor)").mkString("(", " || ", ")")
}
