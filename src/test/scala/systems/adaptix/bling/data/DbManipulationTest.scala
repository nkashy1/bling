package systems.adaptix.bling.data

import org.specs2.mutable._

import scalikejdbc._

/**
 * Created by nkashyap on 6/3/15.
 */
class DbManipulationTest extends Specification {
  sequential

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:mem:test", "lol", "rofl")
  implicit val session = AutoSession

  "Create test table" in {
    sql"CREATE TABLE test (id SERIAL NOT NULL PRIMARY KEY, name TEXT)".execute.apply()
    ok
  }

  "Insert values into test table" in {
    val names = Seq("Akshay", "Bom", "Thor")
    names.foreach(name =>
      sql"INSERT INTO test (name) VALUES (${name})".update.apply()
    )
    ok
  }

  "Select values from test table" in {
    case class TestName(id: Long, name: Option[String])
    object TestNameIfy extends SQLSyntaxSupport[TestName] {
      override val tableName = "test"
      def apply(resultSet: WrappedResultSet) = new TestName(resultSet.long("id"), resultSet.stringOpt("name"))
    }
    val testNames: List[TestName] = sql"SELECT * FROM test".map(resultSet => TestNameIfy(resultSet)).list.apply()

    testNames(0) mustEqual TestName(1, Some("Akshay"))
    testNames(1) mustEqual TestName(2, Some("Bom"))
    testNames(2) mustEqual TestName(3, Some("Thor"))
  }

  "Drop test table" in {
    sql"DROP TABLE test".execute.apply()
    ok
  }
}
