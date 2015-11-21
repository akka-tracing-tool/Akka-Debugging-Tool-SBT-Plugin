package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import sbt.IO._

import scala.io.Source

object FilesGenerator {

  private[this] val aspectTemplateFile = new File(getClass.getClassLoader.getResource("MethodBang.template").getFile)
  private[this] val aopTemplateFile = new File(getClass.getClassLoader.getResource("aop.template").getFile)

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

  def generateResource(configurationParser: ConfigParser, resourcesDir: File): Seq[File] = {
    val hash = configurationParser.hash
    val resourceFile = new File(s"$resourcesDir/META-INF/aop.xml")

    if (resourceFile.exists()) {
      val regex = s"""(?s).*?Configuration hash: <$hash>.*?""".r
      val resourceContents = Source.fromFile(resourceFile).mkString
      resourceContents match {
        case regex(_ *) =>
          return Seq(resourceFile)
        case _ =>
      }
    }

    val packages = configurationParser.getPackages
    val packagesTags = generatePackagesTags(packages)
    val output = readLines(aopTemplateFile).map((line) => {
      if (line.contains("<<<PACKAGES>>>")) {
        line.replace("<<<PACKAGES>>>", packagesTags)
      } else if (line.contains("<<<HASH>>>")) {
        line.replace("<<<HASH>>>", s"<$hash>")
      } else {
        line
      }
    })
    write(resourceFile, output.mkString(Newline))
    Seq(resourceFile)
  }

  private[this] def generateWithin(actors: List[String]): String =
    actors.map(actor => s"within($actor)").mkString("(", " || ", ")")

  private[this] def generatePackagesTags(packages: List[String]): String = {
    packages.map(`package` => s"""<include within="${`package`}..*"/>""").mkString(s"$Newline\t\t")
  }
}
