package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import pl.edu.agh.iet.akka_debugging.sbt.ScalaClassDefinitionParser.parseClassDefinitions

import scala.collection.mutable
import scala.io.Source

object TracedActorsFinder {
  val TRAIT_NAME = "TracedActor"

  def findTracedActors(`package`: String, sources: Seq[File]): List[String] = {
    val packagePath = `package`.replaceAll( """\.""", """/""")
    val actors = mutable.MutableList[String]()

    for (file <- sources) {
      if (file.getAbsoluteFile.getParent.endsWith(packagePath)) {
        val fileContents = Source.fromFile(file).mkString
        val classDefinitions = parseClassDefinitions(fileContents)
        for (classDef <- classDefinitions) {
          if (classDef.traits.contains(TRAIT_NAME)) {
            actors += classDef.name
          }
        }
      }
    }

    actors.toList
  }

}
