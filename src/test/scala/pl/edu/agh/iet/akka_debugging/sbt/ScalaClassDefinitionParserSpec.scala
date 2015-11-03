package pl.edu.agh.iet.akka_debugging.sbt

import org.scalatest.{FlatSpec, Matchers}

class ScalaClassDefinitionParserSpec extends FlatSpec with Matchers {

  import ScalaClassDefinitionParser._

  it should "parse scala class definition" in {
    parseClassDefs("class Test extends Test2 with Test3 with Test4") shouldBe
      List(ClassDef("Test", List("Test2", "Test3", "Test4")))
  }

  it should "parse scala class definition with some stuff before it" in {
    parseClassDefs("<DELETE_ME> class Test extends Test2 with Test3") shouldBe
      List(ClassDef("Test", List("Test2", "Test3")))
  }

  it should "parse scala class definition with some stuff after it" in {
    parseClassDefs("class Test extends Test2 with Test3 <DELETE_ME>") shouldBe
      List(ClassDef("Test", List("Test2", "Test3")))
  }

  it should "parse 2 scala class definitions" in {
    parseClassDefs("class Test extends Test2 with Test3 class A extends B with C with D") shouldBe
      List(ClassDef("Test", List("Test2", "Test3")), ClassDef("A", List("B", "C", "D")))
  }

  it should "parse 2 scala class definitions with stuff before them" in {
    parseClassDefs("<TROLL> class Test extends Test2 with Test3 class A extends B with C with D") shouldBe
      List(ClassDef("Test", List("Test2", "Test3")), ClassDef("A", List("B", "C", "D")))
  }

  it should "parse 2 scala class definitions with stuff between them" in {
    parseClassDefs("class Test extends Test2 with Test3 <TROLL> class A extends B with C with D") shouldBe
      List(ClassDef("Test", List("Test2", "Test3")), ClassDef("A", List("B", "C", "D")))
  }

  it should "parse 2 scala class definitions with stuff after them" in {
    parseClassDefs("class Test(x: Int) extends Test2(x) with Test3 class A extends B with C with D <TROLL>") shouldBe
      List(ClassDef("Test", List("Test2", "Test3")), ClassDef("A", List("B", "C", "D")))
  }

}
