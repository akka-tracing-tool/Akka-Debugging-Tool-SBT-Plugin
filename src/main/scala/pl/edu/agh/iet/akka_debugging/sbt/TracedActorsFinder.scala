package pl.edu.agh.iet.akka_debugging.sbt

import java.io.File

object TracedActorsFinder {

  def findTracedActors(`package`: String, sources: Seq[File]): List[String] = {
    for(file <- sources) {
      println(file.getAbsoluteFile.getParent)
    }

    List()
  }

}
