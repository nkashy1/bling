package systems.adaptix.bling.data

import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll

import scalikejdbc._
import scalikejdbc.config._

/**
 * Created by nkashyap on 6/8/15.
 */
class SelectionCriterionDbTest extends Specification with AfterAll {
  sequential

  DBs.setupAll()
  implicit val session = AutoSession

  val table = SQLSyntax.createUnsafely("SelectionCriterionDbTest_table")
  sql"CREATE TABLE ${table} (ID SERIAL NOT NULL PRIMARY KEY, NAME TEXT, VALUE INT)".execute.apply()

  def afterAll = sql"DROP TABLE ${table}".execute.apply()

  def insertNameValuePair(name: String, value: Int) = sql"INSERT INTO ${table} (name, value) VALUES (${name}, ${value})".update.apply()

  insertNameValuePair("Alice", 42)
  insertNameValuePair("Bob", 0)
  insertNameValuePair("Carol", 5)
  insertNameValuePair("David", 4)
  insertNameValuePair("Edgar", 3478)
  insertNameValuePair("Fahad", -9)

  def selectRowsSubjectTo(constraint: (SQLSyntax, Seq[Any])) = sql"SELECT * FROM ${table} WHERE ${constraint._1}".bind(constraint._2:_*).map(_.toMap).list.apply()

  "Select all rows where value is greater 0." >> {
    val criterion = Gt("value", 0)
    val result = selectRowsSubjectTo(criterion.asSqlSyntaxWithValuesToBind)
    result must haveSize(4)
    result.map(row => row("ID")).toSet mustEqual Set(1, 3, 4, 5)
  }

  "Select all rows where value is greater than 0 or NAME is equal to Bob." >> {
    val criterion = Or(Gt("VALUE", 0), Eq("NAME", "Bob"))
    val result = selectRowsSubjectTo(criterion.asSqlSyntaxWithValuesToBind)
    result must haveSize(5)
    result.map(row => row("ID")).toSet mustEqual Set(1,2,3,4,5)
  }

  "Select all rows which do not satisfy the condition of the previous test." >> {
    val criterion = Not( Or(Gt("VALUE", 0), Eq("NAME", "Bob")) )
    val result = selectRowsSubjectTo(criterion.asSqlSyntaxWithValuesToBind)
    result must haveSize(1)
    result.map(row => row("ID")).toSet mustEqual Set(6)
  }

  "Select all rows where VALUE is negative or all rows where VALUE is 0 and NAME is Bob." >> {
    val criterion = Or( Lt("VALUE", 0), And(Eq("VALUE", 0), Eq("NAME", "Bob")) )
    val result = selectRowsSubjectTo(criterion.asSqlSyntaxWithValuesToBind)
    result must haveSize(2)
    result.map(row => row("ID")).toSet mustEqual Set(2,6)
  }
}
