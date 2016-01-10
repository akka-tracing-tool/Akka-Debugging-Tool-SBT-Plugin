package pl.edu.agh.iet.akka_tracing.sbt

import java.io.{FileWriter, BufferedWriter, File}

import org.scalatest.FlatSpec
import slick.jdbc.meta.MTable
import scala.concurrent.duration._

import scala.concurrent.Await

class DatabaseTasksSpec extends FlatSpec {
  val configFile = File.createTempFile("akka_tracing.conf.test.in-memory-db", "")
  val bufferedWriter = new BufferedWriter(new FileWriter(configFile))
  bufferedWriter.write(
    """
      |akka_tracing {
      |  remote {
      |    database {
      |      driver = "slick.driver.H2Driver$"
      |      db {
      |        driver = "org.h2.Driver"
      |        url = "jdbc:h2:mem:test"
      |      }
      |    }
      |  }
      |}
    """.stripMargin)
  bufferedWriter.close()
  val configParser = new ConfigParser(configFile)
  val databaseTasks = new DatabaseTasks(configParser)

  "A DatabaseTasks \"initDatabase\"" should "create empty tables" in {
    import slick.driver.H2Driver.api._
    val db = Database.forURL("jdbc:h2:mem:test")

    val beforeInitTables = Await.result(db.run(MTable.getTables), 1 seconds).toList
    assert(beforeInitTables.size === 0)

    databaseTasks.initDatabase()

    val afterInitTables = Await.result(db.run(MTable.getTables), 1 seconds).toList
    assert(afterInitTables.size === 2)
    assert(afterInitTables.map(_.name.name) === List("messages", "relation"))
  }

}
