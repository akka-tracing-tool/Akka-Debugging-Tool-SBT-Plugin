package pl.edu.agh.iet.akka_debugging.sbt

import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.CharSequenceReader

trait ScalaClassDefinitionParser extends RegexParsers with ScalaClassDefinitionSyntax {

  val _name = """[a-zA-z~!#%$-+*/\\][a-zA-Z0-9_~!#%$-+*/\\]*""".r
  val _eof = """\z""".r
  val _class = ".*?class".r
  val _with = "with".r
  val _extends = "extends".r

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

  def withClause: Parser[List[String]] = "with" ~ listOfNames ^^ {
    case _ ~ list => list
  }

  def classDef: Parser[ClassDef] = _class ~ name ~ extendsClause.? ^^ {
    case _ ~ className ~ Some(l) =>
      ClassDef(className, l)
    case _ ~ className ~ None =>
      ClassDef(className, List())
  }

}

object ScalaClassDefinitionParser extends ScalaClassDefinitionParser {

  def parseClassDef(s: CharSequence): ClassDef = {
    parseClassDef(new CharSequenceReader(s))
  }

  def parseClassDef(input: CharSequenceReader): ClassDef = {
    parsePhrase(input) match {
      case Success(t, _) => t
      case NoSuccess(msg, next) => throw new IllegalArgumentException(
        "Could not parse '" + input + "' near '" + next.pos.longString + ": " + msg)
    }
  }

  def parsePhrase(input: CharSequenceReader): ParseResult[ClassDef] = {
    phrase(classDef)(input)
  }

}

trait ScalaClassDefinitionSyntax {

  case class ClassDef(name: String, traits: List[String])

}
