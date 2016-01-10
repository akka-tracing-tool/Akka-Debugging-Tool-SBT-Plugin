package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec

class ImplicitsSpec extends FlatSpec {
  val configFile = new File(getClass.getResource("/akka_tracing.conf.test").toURI)
  val config = ConfigFactory.parseFile(configFile)

  import Implicits._

  "A RichConfig" should "return correct values when executing \"getStringListOr\" on Config" in {
    assert(config.getStringListOr("akka_tracing.packages", List("")) ===
      List("pl.edu.agh.iet.akka_tracing.test.actors1", "pl.edu.agh.iet.akka_tracing.test.actors2"))
  }

  it should "return \"or\" argument when key doesn't exist" in {
    assert(config.getStringListOr("NON_EXISTING_KEY", List("*")) === List("*"))
  }
}
