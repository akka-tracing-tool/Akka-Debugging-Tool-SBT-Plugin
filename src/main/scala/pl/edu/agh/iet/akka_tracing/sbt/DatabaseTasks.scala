package pl.edu.agh.iet.akka_tracing.sbt

import java.io.File

import sbt._

object DatabaseTasks {
  private val collectorClass = "pl.edu.agh.iet.akka_tracing.collector.PluginOperations"

  def initDatabase(classPath: Seq[File], logger: Logger, configFileName: String): Unit = {
    runProcess(classPath, logger, "init", configFileName)
  }

  def cleanDatabase(classPath: Seq[File], logger: Logger, configFileName: String): Unit = {
    runProcess(classPath, logger, "clean", configFileName)
  }

  private def runProcess(classPath: Seq[File],
                         logger: Logger,
                         task: String,
                         configFileName: String): Unit = {
    Process(
      s"java -cp ${classPath.map(_.getAbsolutePath).mkString(":")} $collectorClass $task $configFileName"
    ).run(logger).exitValue()
  }
}
