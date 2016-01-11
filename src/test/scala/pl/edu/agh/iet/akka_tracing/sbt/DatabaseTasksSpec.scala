package pl.edu.agh.iet.akka_tracing.sbt

import java.io.{FileWriter, BufferedWriter, File}
import java.util.UUID
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import slick.jdbc.meta.MTable
import scala.concurrent.duration._
import scala.concurrent.Await

class DatabaseTasksSpec extends FlatSpec with BeforeAndAfterEach {
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

  """A DatabaseTasks "initDatabase"""" should "create empty tables" in {
    val beforeInitTables = Await.result(db.run(MTable.getTables), 1 seconds).toList

    assert(beforeInitTables.size === 0)

    databaseTasks.initDatabase()
    val afterInitTables = Await.result(db.run(MTable.getTables), 1 seconds).toList

    assert(afterInitTables.size === 2)
    assert(afterInitTables.map(_.name.name) === List("messages", "relation"))
  }

  it should """do nothing when "initDatabase" method called more than once""" in {
    databaseTasks.initDatabase()
    insertData

    databaseTasks.initDatabase()

    val afterMessagesRowsCount = Await.result(db.run(countMessagesQuery), 1 seconds).head
    val afterRelationRowsCount = Await.result(db.run(countRelationQuery), 1 seconds).head

    assert(afterMessagesRowsCount === 1)
    assert(afterRelationRowsCount === 1)
  }

  """A DatabaseTasks "cleanDatabase"""" should """remove rows from tables "messages" & "relation"""" in {
    databaseTasks.initDatabase()
    insertData

    val beforeMessagesRowsCount = Await.result(db.run(countMessagesQuery), 1 seconds).head
    val beforeRelationRowsCount = Await.result(db.run(countRelationQuery), 1 seconds).head

    assert(beforeMessagesRowsCount === 1)
    assert(beforeRelationRowsCount === 1)

    databaseTasks.cleanDatabase()

    val afterMessagesRowsCount = Await.result(db.run(countMessagesQuery), 1 seconds).head
    val afterRelationRowsCount = Await.result(db.run(countRelationQuery), 1 seconds).head

    assert(afterMessagesRowsCount === 0)
    assert(afterRelationRowsCount === 0)
  }

  it should "not remove tables from database" in {
    databaseTasks.initDatabase()
    databaseTasks.cleanDatabase()
    val tables = Await.result(db.run(MTable.getTables), 1 seconds).toList

    assert(tables.size === 2)
    assert(tables.map(_.name.name) === List("messages", "relation"))
  }

  override protected def afterEach(): Unit = {
    Await.result(db.run(DBIO.seq(
      sqlu"""drop table if exists "messages"""",
      sqlu"""drop table if exists "relation""""
    )), 1 seconds)
  }

  def insertData: Unit = {
    val randomUUID = UUID.randomUUID()
    Await.result(db.run(DBIO.seq(
      sqlu"""insert into "messages" values(${randomUUID.toString}, 'sender', 'receiver')""",
      sqlu"""insert into "relation" values(${randomUUID.toString}, ${randomUUID.toString})"""
    )), 1 seconds)
  }

  val countMessagesQuery = sql"""select count(*) from "messages"""".as[Int]
  val countRelationQuery = sql"""select count(*) from "relation"""".as[Int]
}
