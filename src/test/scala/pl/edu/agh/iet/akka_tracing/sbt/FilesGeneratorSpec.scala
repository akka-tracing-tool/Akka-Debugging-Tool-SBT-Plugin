package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class FilesGeneratorSpec extends FlatSpec with BeforeAndAfterEach {
  val configFile = new File(getClass.getResource("/akka_tracing.conf.test").toURI)
  val configParser = ConfigParser(configFile)

  "A FilesGenerator" should """generate aspect in "scala/akka"" package""" in {
    val output = FilesGenerator.generateAspect(configParser, new File("."), "CONFIG_NAME").head
    assert(new File("./scala/akka/MethodBang.scala").exists() === true)
    assert(output === new File("./scala/akka/MethodBang.scala"))
  }

  override protected def afterEach(): Unit = {
    val aspectFile = new File("./scala/akka/MethodBang.scala")
    if(aspectFile.exists()) {
      aspectFile.delete()
    }
    //todo remove whole scala/akka directory when there are no other files
  }
}
