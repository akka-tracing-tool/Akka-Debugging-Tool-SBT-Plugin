package pl.edu.agh.iet.akka_tracing.sbt

import java.io.{FileWriter, BufferedWriter, File}
import java.util.UUID

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

  import slick.driver.H2Driver.api._
  val db = Database.forURL("jdbc:h2:mem:test")

  "A DatabaseTasks \"initDatabase\"" should "create empty tables" in {
    val beforeInitTables = Await.result(db.run(MTable.getTables), 1 seconds).toList
    assert(beforeInitTables.size === 0)

    databaseTasks.initDatabase()
    val afterInitTables = Await.result(db.run(MTable.getTables), 1 seconds).toList
    assert(afterInitTables.size === 2)
    assert(afterInitTables.map(_.name.name) === List("messages", "relation"))
  }

  it should "remove rows from tables \"messages\" & \"relation\"" in {
    val randomUUID = UUID.randomUUID()
    databaseTasks.initDatabase()
    Await.result(db.run(DBIO.seq(
      sqlu"""insert into "messages" values(${randomUUID.toString}, 'sender', 'receiver')""",
      sqlu"""insert into "relation" values(${randomUUID.toString}, ${randomUUID.toString})"""
    )), 1 seconds)

    val resultMessages = Await.result(db.run(sql"""select count(*) from "messages"""".as[Int]), 1 seconds).head
    val resultRelation = Await.result(db.run(sql"""select count(*) from "relation"""".as[Int]), 1 seconds).head
    assert(resultMessages === 1)
    assert(resultRelation === 1)

    databaseTasks.cleanDatabase()
    val afterResultMessages = Await.result(db.run(sql"""select count(*) from "messages"""".as[Int]), 1 seconds).head
    val afterResultRelation = Await.result(db.run(sql"""select count(*) from "relation"""".as[Int]), 1 seconds).head
    assert(afterResultMessages === 0)
    assert(afterResultRelation === 0)
//    val afterInitTables = Await.result(db.run(MTable.getTables), 1 seconds).toList
  }

}
