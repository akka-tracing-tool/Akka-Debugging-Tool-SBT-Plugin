package pl.edu.agh.iet.akka_debugging.sbt

import org.scalatest.{Matchers, FlatSpec}

class ScalaClassDefinitionParserSpec extends FlatSpec with Matchers {

  import ScalaClassDefinitionParser._

  it should "parse scala class definitions" in {
    parseClassDef("class Test extends Test2 with Test3") shouldBe ClassDef("Test", List("Test2", "Test3"))
    parseClassDef("<DELETE_ME> class Test extends Test2 with Test3") shouldBe ClassDef("Test", List("Test2", "Test3"))
  }

}
