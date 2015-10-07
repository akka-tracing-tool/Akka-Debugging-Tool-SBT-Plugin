package pl.edu.agh.iet.akka_debugging.sbt

import org.reflections.Reflections

import scala.collection.JavaConverters._

object ReflectionUtils {
  def getClassesFromPackage(subclass: Class[_], `package`: String): List[Class[_]] = {
    val reflections: Reflections = new Reflections(`package`)
    reflections.getSubTypesOf(subclass).asScala.toList
  }
}
