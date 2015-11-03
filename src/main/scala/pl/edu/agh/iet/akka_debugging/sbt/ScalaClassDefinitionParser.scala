package pl.edu.agh.iet.akka_debugging.sbt

import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.parsing.input.CharSequenceReader

trait ScalaClassDefinitionParser extends JavaTokenParsers with ScalaClassDefinitionSyntax {

  val _name = ident
  val _class = ".*?class".r
  val _with = "with".r
  val _extends = ".*?extends".r
  val _anything = ".*".r
  val _first_with = ".*?with".r

  def name: Parser[String] = _name ^^ {
    s => s
  }

  def listOfNames: Parser[List[String]] = repsep(name, _with) ^^ {
    names: List[String] => names
  }

  def extendsClause: Parser[List[String]] = (_extends ~ name ~ withClause.?) ^^ {
    case _ ~ name ~ Some(traits) => name :: traits
    case _ ~ name ~ None => List(name)
  }

  def withClause: Parser[List[String]] = _first_with ~ listOfNames ^^ {
    case _ ~ list => list
  }

  def classDef: Parser[ClassDef] = _class ~ name ~ extendsClause.? ^^ {
    case _ ~ className ~ Some(l) =>
      ClassDef(className, l)
    case _ ~ className ~ None =>
      ClassDef(className, List())
  }

  def classDefinitions: Parser[List[ClassDef]] = rep(classDef) ~ _anything ^^ {
    case l ~ _ => l
  }

}

object ScalaClassDefinitionParser extends ScalaClassDefinitionParser {

  def parseClassDefs(s: CharSequence): List[ClassDef] = {
    parseClassDefs(new CharSequenceReader(s))
  }

  def parseClassDefs(input: CharSequenceReader): List[ClassDef] = {
    parsePhrase(input) match {
      case Success(t, _) => t
      case NoSuccess(_, _) => List()
    }
  }

  def parsePhrase(input: CharSequenceReader): ParseResult[List[ClassDef]] = {
    phrase(classDefinitions)(input)
  }

}

trait ScalaClassDefinitionSyntax {

  case class ClassDef(name: String, traits: List[String])

}
