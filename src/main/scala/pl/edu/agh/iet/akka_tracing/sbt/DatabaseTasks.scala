package pl.edu.agh.iet.akka_tracing.sbt

import pl.edu.agh.iet.akka_tracing.database.DatabaseUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DatabaseTasks(val configParser: ConfigParser)
  extends DatabaseUtils(configParser.getDatabaseConfig) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def initDatabase(): Unit = {
    Await.result(init, Duration.Inf)
    Thread.sleep(3000)
  }

  def cleanDatabase(): Unit = {
    val db = dc.db

    import dc.driver.api._

    val messages = TableQuery[CollectorDBMessages]
    val relation = TableQuery[CollectorDBMessagesRelations]

    Await.result(db.run(DBIO.seq(
      messages.delete,
      relation.delete
    )), Duration.Inf)
    Thread.sleep(3000)
  }
}
