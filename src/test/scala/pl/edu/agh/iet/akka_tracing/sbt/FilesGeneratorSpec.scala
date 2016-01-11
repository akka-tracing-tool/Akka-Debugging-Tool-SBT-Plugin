package pl.edu.agh.iet.akka_tracing.sbt

import java.io.{FileReader, BufferedReader, File}

import org.scalatest.{BeforeAndAfterEach, FlatSpec}

import scala.io.Source

class FilesGeneratorSpec extends FlatSpec with BeforeAndAfterEach {
  val configFile = new File(getClass.getResource("/akka_tracing.conf.test").toURI)
  val configFile2 = new File(getClass.getResource("/akka_tracing.conf.test_empty").toURI)
  val configParser = ConfigParser(configFile)

  "A FilesGenerator" should """generate aspect in "scala/akka"" package""" in {
    val output = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    assert(new File("./scala/akka/MethodBang.scala").exists() === true)
    assert(output === new File("./scala/akka/MethodBang.scala"))
  }

  it should "not generate aspect with the same configuration" in {
    val output = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    val lines = Source.fromFile(output).mkString
    val output2 = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    val lines2 = Source.fromFile(output).mkString
    assert(lines === lines2)
  }

  it should "generate aspect when configuration changed" in {
    val output = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    val lines = Source.fromFile(output).mkString
    val output2 = FilesGenerator.generateAspect(ConfigParser(configFile2), new File("."), "CONFIG_NAME").head
    val lines2 = Source.fromFile(output).mkString
    assert(lines !== lines2)
  }

  override protected def afterEach(): Unit = {
    val aspectFile = new File("./scala/akka/MethodBang.scala")
    if(aspectFile.exists()) {
      aspectFile.delete()
    }
    //todo remove whole scala/akka directory when there are no other files
  }
}
