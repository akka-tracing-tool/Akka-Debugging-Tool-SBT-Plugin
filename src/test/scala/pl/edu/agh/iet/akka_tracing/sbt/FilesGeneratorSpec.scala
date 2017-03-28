package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import org.apache.commons.codec.digest.DigestUtils
import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import sbt.IO._

class FilesGeneratorSpec extends FlatSpec with BeforeAndAfterEach {
  val configFile = new File(getClass.getResource("/akka_tracing.conf.test").toURI)
  val configFile2 = new File(getClass.getResource("/akka_tracing.conf.test_empty").toURI)

  "A FilesGenerator" should """generate aspect in "scala/akka"" package""" in {
    val output = FilesGenerator.generateAspect(configFile, new File("."), "CONFIG_NAME").head
    val expected = new File("./scala/akka/AkkaTracingAspect.scala")
    assert(expected.exists() === true)
    assert(output === expected)
  }

  it should "not generate aspect with the same configuration" in {
    val output = FilesGenerator.generateAspect(configFile, new File("."), "CONFIG_NAME").head
    val output2 = FilesGenerator.generateAspect(configFile, new File("."), "CONFIG_NAME").head
    assert(output.lastModified() === output2.lastModified())
  }

  it should "generate aspect when configuration changed" in {
    val output = FilesGenerator.generateAspect(configFile, new File("."), "CONFIG_NAME").head
    val lines = readLines(output)
    val output2 = FilesGenerator.generateAspect(configFile2, new File("."), "CONFIG_NAME").head
    val lines2 = readLines(output2)
    assert(lines !== lines2)
  }

  it should "generate correct aspect from template (hash & config's filename)" in {
    val output = FilesGenerator.generateAspect(configFile, new File("."), "CONFIG_NAME").head
    val lines = readLines(output).mkString
    val hash = DigestUtils.sha512Hex(readLines(configFile).mkString)
    assert(lines.contains(hash) === true)
    assert(lines.contains("CONFIG_NAME"))
  }

  it should """generate "aop.xml" resource in  "META-INF" directory""" in {
    val output = FilesGenerator.generateResource(configFile, new File(".")).head
    val expected = new File("./META-INF/aop.xml")
    assert(expected.exists() === true)
    assert(output === expected)
  }

  it should """not generate "aop.xml" with the same configuration""" in {
    val output = FilesGenerator.generateResource(configFile, new File(".")).head
    val output2 = FilesGenerator.generateResource(configFile, new File(".")).head
    assert(output.lastModified() === output2.lastModified())
  }

  it should """generate "aop.xml"" when configuration changed""" in {
    val output = FilesGenerator.generateResource(configFile, new File(".")).head
    val lines = readLines(output)
    val output2 = FilesGenerator.generateResource(configFile2, new File(".")).head
    val lines2 = readLines(output2)
    assert(lines !== lines2)
  }

  it should "generate correct resource from template (hash & packages)" in {
    val output = FilesGenerator.generateResource(configFile, new File(".")).head
    val lines = readLines(output).mkString
    val hash = DigestUtils.sha512Hex(readLines(configFile).mkString)
    assert(lines.contains(hash) === true)
    val configurationReader = new ConfigurationReader(configFile)
    configurationReader.getPackages foreach { `package` =>
      assert(lines.contains(s"""<include within="${`package`}..*" />"""))
    }
  }

  override protected def afterEach(): Unit = {
    val aspectFile = new File("./scala/akka/AkkaTracingAspect.scala")
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
