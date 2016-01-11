package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import com.typesafe.config.ConfigException
import org.scalatest.FlatSpec

class ConfigParserSpec extends FlatSpec {
  val file = new File(getClass.getResource("/akka_tracing.conf.test").toURI)
  val fileEmptyPackages = new File(getClass.getResource("/akka_tracing.conf.test_empty").toURI)
  val fileNoPackages = new File(getClass.getResource("/akka_tracing.conf.test_no").toURI)

  "A ConfigParser" should "return correct package names" in {
    assert(ConfigParser(fileEmptyPackages).getPackages === List())
    assert(ConfigParser(file).getPackages ===
      List("pl.edu.agh.iet.akka_tracing.test.actors1", "pl.edu.agh.iet.akka_tracing.test.actors2"))
  }

  it should "throw ConfigException.Missing when executed \"getPackages\" with missing \"packages\" key" in {
    intercept[ConfigException.Missing] {
      ConfigParser(fileNoPackages).getPackages
    }
  }

  it should "return correct actor names" in {
    assert(ConfigParser(file).getActors ===
      List("pl.edu.agh.iet.akka_tracing.test.actors1.Actor1", "pl.edu.agh.iet.akka_tracing.test.actors2.*"))
  }

  it should "throw ConfigException.Missing when executed \"getActor\" with missing \"packages\" key" in {
    intercept[ConfigException.Missing] {
      ConfigParser(fileNoPackages).getActors
    }
  }
}
