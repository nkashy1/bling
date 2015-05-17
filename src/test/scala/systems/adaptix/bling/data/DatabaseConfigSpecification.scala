package systems.adaptix.bling.data

import org.specs2.mutable._
import org.specs2.specification._
import org.specs2.concurrent.ExecutionEnv

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import slick.dbio.DBIO
import slick.lifted.{Tag, TableQuery}

/**
 * Created by nkashyap on 5/15/15.
 */
class DatabaseConfigSpecification extends Specification with AfterAll {
  val db = Database.forConfig("blingtestdb")

  case class TestDatum(key: Option[Int], value: String)
  class Test(tag: Tag) extends Table[TestDatum](tag, "TEST") {
    def key = column[Int]("KEY", O.PrimaryKey, O.AutoInc)
    def value = column[String]("VALUE")

    def * = (key.?, value) <> (TestDatum.tupled, TestDatum.unapply)
  }
  val test = TableQuery[Test]
  val setup = DBIO.seq(
    test.schema.create
  )
  db.run(setup)

  def afterAll = {
    db.run(test.schema.drop)
    db.close()
  }

  "bling should be able to connect to and manipulate a database specified in application.conf using the Slick configuration format." >> {
    implicit ee: ExecutionEnv =>
    val entries = Seq(
      TestDatum(None, "Alice"),
      TestDatum(None, "Bob"),
      TestDatum(None, "Carol"),
      TestDatum(None, "Dave"),
      TestDatum(None, "Edgar")
    )

    val insertions = DBIO.seq(
      test ++= entries
    )
    val selectQuery = for(pair <- test) yield pair

    val insertion = db.run(insertions)
    val selection = insertion flatMap {_ => db.run(selectQuery.result)}
    val expectedResult = Vector(
      TestDatum(Some(1), "Alice"),
      TestDatum(Some(2), "Bob"),
      TestDatum(Some(3), "Carol"),
      TestDatum(Some(4), "Dave"),
      TestDatum(Some(5), "Edgar")
    )

    selection must be_==(expectedResult).await
  }
}
