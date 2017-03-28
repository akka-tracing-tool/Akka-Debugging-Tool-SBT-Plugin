package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import org.apache.commons.codec.digest.DigestUtils
import sbt.IO._
import sbt._

object FilesGenerator {

  private[this] val aspectTemplateURL = getClass.getResource("/AkkaTracingAspect.template")
  private[this] val aopTemplateURL = getClass.getResource("/aop.template")

  def generateAspect(configFile: File,
                     sourcesDir: File,
                     configFileName: String): Seq[File] = {
    val configuration = readLines(configFile).mkString
    val hash = DigestUtils.sha512Hex(configuration)
    val aspectFile = sourcesDir / "scala" / "akka" / "AkkaTracingAspect.scala"

    if (aspectFile.exists()) {
      val regex = s"""(?s).*?Configuration hash: <$hash>.*?""".r
      val aspectContents = readLines(aspectFile).mkString
      aspectContents match {
        case regex(_ *) =>
          return Seq(aspectFile)
        case _ =>
      }
    }

    val output = readLinesURL(aspectTemplateURL).map((line) => {
      if (line.contains("<<<HASH>>>")) {
        line.replace("<<<HASH>>>", s"<$hash>")
      } else if (line.contains("<<<CONFIG>>>")) {
        line.replace("<<<CONFIG>>>", configFileName)
      } else {
        line
      }
    })
    write(aspectFile, output.mkString(Newline))
    Seq(aspectFile)
  }

  def generateResource(configFile: File,
                       resourcesDir: File): Seq[File] = {
    val configuration = readLines(configFile).mkString
    val hash = DigestUtils.sha512Hex(configuration)
    val resourceFile = resourcesDir / "META-INF" / "aop.xml"

    if (resourceFile.exists()) {
      val regex = s"""(?s).*?Configuration hash: <$hash>.*?""".r
      val resourceContents = readLines(resourceFile).mkString
      resourceContents match {
        case regex(_ *) =>
          return Seq(resourceFile)
        case _ =>
      }
    }

    val packages = new ConfigurationReader(configFile).getPackages

    val output = readLinesURL(aopTemplateURL).map((line) => {
      if (line.contains("<<<HASH>>>")) {
        line.replace("<<<HASH>>>", s"<$hash>")
      } else if (line.contains("<<<PACKAGES>>>")) {
        line.replace(
          "<<<PACKAGES>>>",
          packages.map(p => s"""<include within="$p..*" />""").mkString("\n        ")
        )
      } else {
        line
      }
    })
    write(resourceFile, output.mkString(Newline))
    Seq(resourceFile)
  }
}
