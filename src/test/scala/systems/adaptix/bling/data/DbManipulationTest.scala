package systems.adaptix.bling.data

import org.specs2.mutable._

import scalikejdbc._
import scalikejdbc.config._

/**
 * Created by nkashyap on 6/3/15.
 */
class DbManipulationTest extends Specification {
  sequential

  DBs.setupAll()
  implicit val session = AutoSession

  "Create test table" in {
    sql"CREATE TABLE test (id SERIAL NOT NULL PRIMARY KEY, name TEXT)".execute.apply()
    ok
  }

  "Insert values into test table" in {
    val names = Seq("Akshay", "Bom", "Thor")
    var ids = Seq[Long]()
    for (i <- 0 to 2) {
      ids = ids :+ sql"INSERT INTO test (name) VALUES (${names(i)})".updateAndReturnGeneratedKey().apply()
    }
    ids(0) mustEqual 1
    ids(1) mustEqual 2
    ids(2) mustEqual 3
  }

  case class TestName(id: Long, name: Option[String])
  object TestName extends SQLSyntaxSupport[TestName] {
    override val tableName = "test"
    def apply(resultSet: WrappedResultSet) = new TestName(resultSet.long("id"), resultSet.stringOpt("name"))
  }

  "Select values from test table" in {
    val testNames: List[TestName] = sql"SELECT * FROM test".map(resultSet => TestName(resultSet)).list.apply()

    testNames(0) mustEqual TestName(1, Some("Akshay"))
    testNames(1) mustEqual TestName(2, Some("Bom"))
    testNames(2) mustEqual TestName(3, Some("Thor"))
  }

  "Drop test table" in {
    sql"DROP TABLE test".execute.apply()
    ok
  }
}
