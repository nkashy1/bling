package systems.adaptix.bling.data

import org.specs2.mutable.Specification

import scalikejdbc._
import scalikejdbc.config._

/**
 * Created by nkashyap on 6/4/15.
 */

class TableTemplateSpecification extends Specification {
  sequential

  DBs.setupAll()
  implicit val session = AutoSession

  "A TableTemplate object generates SQL code which can be executed to access and update the table it represents in a given database. The point is that a TableTemplate object can be created at run time, and the table schema therefore does not have to be known at compile time." >> {
    val tableName = "DataTableTemplateSpecification_table"
    val idColumn = AutoIdFieldInfo("id")
    val nameColumn = PlainFieldInfo("name", "VARCHAR")

    val testTable = new TableTemplate(tableName, Seq(idColumn, nameColumn))

    "It is instantiated with the tableName, and a sequence of FieldInfo objects representing the columns of the table. These respectively form the tableName and columns members of the TableTemplate object." >> {
      testTable.tableName mustEqual tableName
      testTable.columns mustEqual Seq(idColumn, nameColumn)
    }

    "The schema method returns the table schema as a string." >> {
      testTable.schema mustEqual "id SERIAL NOT NULL PRIMARY KEY, name VARCHAR"
    }

    "The columnNames method returns the names of the columns (without parentheses) in the order in which they were provided to the TableTemplate object at instantiation." >> {
      testTable.columnNames mustEqual "id, name"
    }

    "The nonAutoIdColumnNames method returns the names of the columns which are NOT automatically generated ID columns (without parentheses) in the order in which they were provided to the TableTemplate object at instantiation." >> {
      testTable.nonAutoIdColumnNames mustEqual "name"
    }

    "The sqlCreate method returns an SQLSyntax object which can be executed within an sql interpolation to actually create the table in a given database. The create method performs this execution." >> {
      val creationString = s"CREATE TABLE ${tableName} (id SERIAL NOT NULL PRIMARY KEY, name VARCHAR)"
      testTable.sqlCreate mustEqual SQLSyntax.createUnsafely(creationString)
    }

    testTable.create

    val names = Seq("Akshay", "Bom", "Thor")
    var ids = Seq[Long]()
    for (i <- 0 to 2) {
      ids = ids :+ sql"INSERT INTO ${testTable.sqlTableName} (${testTable.sqlNonAutoIdColumnNames}) VALUES (${names(i)})".updateAndReturnGeneratedKey().apply()
    }
    ids(0) mustEqual 1
    ids(1) mustEqual 2
    ids(2) mustEqual 3

    val testNames: List[TestName] = sql"SELECT * FROM ${testTable.sqlTableName}".map(resultSet => TestName(resultSet)).list.apply()
    testNames(0) mustEqual TestName(1, Some("Akshay"))
    testNames(1) mustEqual TestName(2, Some("Bom"))
    testNames(2) mustEqual TestName(3, Some("Thor"))

    "The sqlDrop method returns an SQLSyntax object which can be executed within an sql interpolation to drop the table from the active database. The drop method performs this execution." in {
      val dropString = s"DROP TABLE ${tableName}"
      testTable.sqlDrop mustEqual SQLSyntax.createUnsafely(dropString)
    }

    testTable.drop

    "The sqlTableName, sqlColumnNames, and sqlNonAutoIdColumnNames methods create SQLSyntax objects out of the strings returned by the tableName, columnNames, and nonAutoIdColumnNames methods respectively." >> {
      ok
    }

    "In the process, we have replicated DbManipulationTest in a more dynamic way." >> {
      ok
    }
  }
}

case class TestName(id: Long, name: Option[String])
object TestName extends SQLSyntaxSupport[TestName] {
  override val tableName = "test"
  def apply(resultSet: WrappedResultSet) = new TestName(resultSet.long("id"), resultSet.stringOpt("name"))
}
