package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

import pl.edu.agh.iet.akka_debugging.sbt.ScalaClassDefinitionParser._

import scala.collection.mutable
import scala.io.Source

object TracedActorsFinder {

  //TODO: Change it to new class name
  val TRAIT_NAME = "DistributedStackTrace"

  def findTracedActors(`package`: String, sources: Seq[File]): List[String] = {

    val packagePath = `package`.replaceAll( """\.""", """/""")

    val actors = mutable.MutableList[String]()

    for (file <- sources) {
      if (file.getAbsoluteFile.getParent.endsWith(packagePath)) {
        val fileContents = Source.fromFile(file).mkString
        val classDefs = parseClassDefs(fileContents)
        for (classDef <- classDefs) {
          if (classDef.traits.contains(TRAIT_NAME)) {
            actors += classDef.name
          }
        }
      }
    }

    List(actors: _*)
  }

}
