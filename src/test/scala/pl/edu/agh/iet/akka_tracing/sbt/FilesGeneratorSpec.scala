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
    val output2 = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    assert(output.lastModified() === output2.lastModified())
  }

  it should "generate aspect when configuration changed" in {
    val output = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    val lines = Source.fromFile(output).mkString
    val output2 = FilesGenerator.generateAspect(ConfigParser(configFile2), new File("."), "CONFIG_NAME").head
    val lines2 = Source.fromFile(output2).mkString
    assert(lines !== lines2)
  }

  it should "generate correct aspect from template (hash, actors & config's filename)" in {
    val output = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    val lines = Source.fromFile(output).mkString
    assert(lines.contains(s"${configParser.hash}") === true)
    for(actor <- configParser.getActors)
      assert(lines.contains(s"within($actor)"))
    assert(lines.contains("CONFIG_NAME"))
  }

  it should """generate "aop.xml" resource in  "META-INF" directory""" in {
    val output = FilesGenerator.generateResource(configParser, new File(".")).head
    assert(new File("./META-INF/aop.xml").exists() === true)
    assert(output === new File("./META-INF/aop.xml"))
  }

  it should """not generate "aop.xml" with the same configuration""" in {
    val output = FilesGenerator.generateResource(configParser, new File(".")).head
    val output2 = FilesGenerator.generateResource(configParser, new File(".")).head
    assert(output.lastModified() === output2.lastModified())
  }

  it should """generate "aop.xml"" when configuration changed""" in {
    val output = FilesGenerator.generateResource(configParser, new File(".")).head
    val lines = Source.fromFile(output).mkString
    val output2 = FilesGenerator.generateResource(ConfigParser(configFile2), new File(".")).head
    val lines2 = Source.fromFile(output2).mkString
    assert(lines !== lines2)
  }

  it should "generate correct resource from template (hash & packages)" in {
    val output = FilesGenerator.generateResource(configParser, new File(".")).head
    val lines = Source.fromFile(output).mkString
    assert(lines.contains(s"${configParser.hash}") === true)
    for(`package` <- configParser.getPackages)
      assert(lines.contains(s"""<include within="${`package`}..*"/>"""))
  }

  override protected def afterEach(): Unit = {
    val aspectFile = new File("./scala/akka/MethodBang.scala")
    if(aspectFile.exists()) {
      aspectFile.delete()
    }

    val resourceFile = new File("./META-INF/aop.xml")
    if(resourceFile.exists()) {
      resourceFile.delete()
    }
    //todo remove whole scala/akka & META-INF directories when there are no other files
  }
}
