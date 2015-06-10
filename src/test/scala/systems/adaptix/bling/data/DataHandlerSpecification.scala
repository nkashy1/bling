package systems.adaptix.bling.data

import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll

import scalikejdbc._
import scalikejdbc.config._

/**
 * Created by nkashyap on 6/5/15.
 */
class DataHandlerSpecification extends Specification with AfterAll {
  sequential

  DBs.setupAll()
  implicit val session = AutoSession

  val id = AutoIdFieldInfo("ID")
  val name = PlainFieldInfo("NAME", "VARCHAR")
  val number = PlainFieldInfo("RANDOM", "INT")
  val dataTemplate = new TableTemplate("AbsorberSpecification_data", Seq(id, name, number))

  val tag = PrimaryFieldInfo("TAG", "VARCHAR")
  val tagsTemplate = new TagTableTemplate("AbsorberSpecification_tags", "TAG")

  val dataHandler = new DataHandler(dataTemplate, tagsTemplate)

  def afterAll = {
    sql"${dataTemplate.sqlDrop}".execute.apply()
    sql"${tagsTemplate.sqlDrop}".execute.apply()
    dataHandler.tagIndexers.foreach(pair => sql"${pair._2.sqlDrop}".execute.apply())
  }

  "The DataHandler class allows TaggedData to be loaded into and extracted from the table specified by a given TableTemplate in a database, with the tags being registered in a table specified by a TagTableTemplate." >> {
    "The dataTemplate and tagsTemplate TableTemplates specifying the schema of the data table and the tags table respectively are passed at instantiation." >> {
      dataHandler.dataTemplate mustEqual dataTemplate
      dataHandler.tagsTemplate mustEqual tagsTemplate
    }

    "With a fresh database, the DataHandler object's tagIndexers method should be empty." >> {
      dataHandler.tagIndexers must beEmpty
    }

    "The insert method loads data into the specified data table and adds the generated ID for the given data to the relevant tag indexers." >> {
      val input1 = TaggedData(Map[String, Any]("NAME" -> "bob"), Set("lol"))
      dataHandler.insert(input1)
      dataHandler.tagIndexers.keySet mustEqual Set("lol")

      sql"SELECT ID FROM ${dataHandler.tagIndexers("lol").sqlTableName}".map(_.toMap).first.apply() mustEqual Some(Map[String, Any]("ID" -> 1))

      var dataTableSelection = sql"SELECT * FROM ${dataHandler.dataTemplate.sqlTableName}".map(_.toMap).list.apply()
      dataTableSelection must haveSize(1)
      dataTableSelection(0) mustEqual Map[String, Any]("ID" -> 1, "NAME" -> "bob")

      val input2 = TaggedData(Map[String, Any]("NAME" -> "alice", "RANDOM" -> 4), Set("lol", "rofl"))
      dataHandler.insert(input2)
      dataHandler.tagIndexers.keySet mustEqual Set("lol", "rofl")

      val lolSelection = sql"SELECT ID FROM ${dataHandler.tagIndexers("lol").sqlTableName}".map(_.toMap).list.apply().toSet
      lolSelection must haveSize(2)
      lolSelection must contain(Map[String, Any]("ID" -> 1))
      lolSelection must contain(Map[String, Any]("ID" -> 2))

      val roflSelection = sql"SELECT ID FROM ${dataHandler.tagIndexers("rofl").sqlTableName}".map(_.toMap).list.apply().toSet
      roflSelection must haveSize(1)
      roflSelection must contain(Map[String, Any]("ID" -> 2))

      dataTableSelection = sql"SELECT * FROM ${dataHandler.dataTemplate.sqlTableName}".map(_.toMap).list.apply()
      dataTableSelection must haveSize(2)
      dataTableSelection(0) mustEqual Map[String, Any]("ID" -> 1, "NAME" -> "bob")
      dataTableSelection(1) mustEqual Map[String, Any]("ID" -> 2, "NAME" -> "alice", "RANDOM" -> 4)

      val tagsTableSelection = sql"SELECT * FROM ${dataHandler.tagsTemplate.sqlTableName}".map(_.toMap).list.apply().toSet
      tagsTableSelection must haveSize(2)
      tagsTableSelection must contain(Map("TAG" -> "lol"))
      tagsTableSelection must contain(Map("TAG" -> "rofl"))
    }

    "validateFields is a utility method that can be used to check if a given TaggedData object is appropriate for absorption in terms of the fields that it inserts values into." >> {
      val validInput = TaggedData(Map[String, Any]("NAME" -> "bob", "RANDOM" -> 42), Set("lol"))
      dataHandler.validateFields(validInput) must beTrue

      val invalidInput = TaggedData(Map[String, Any]("lol" -> "rofl", "NAME" -> "divid"), Set("rofl"))
      dataHandler.validateFields(invalidInput) must beFalse
    }

    "The loadTagIndexers method allows the tagIndexers member variable to be updated to reflect independent updates to the database." >> {
      dataHandler.loadTagIndexers
      dataHandler.tagIndexers.keySet mustEqual Set("lol", "rofl")
    }

    "The select method allows data to be extracted from a given table, which is the data table by default." >> {
      val fullData = dataHandler.select(AllColumns)
      fullData.map(row => row("ID")).toSet mustEqual Set(1,2)

      val lolIndexer = dataHandler.tagIndexers("lol")
      val lolData = dataHandler.select( AllColumns, In("ID", lolIndexer.tableName, SomeColumns(Seq(lolIndexer.columnName))))
      lolData.map(row => row("ID")).toSet mustEqual Set(1,2)

      val roflIndexer = dataHandler.tagIndexers("rofl")
      val roflData = dataHandler.select( AllColumns, In("ID", roflIndexer.tableName, AllColumns) )
      roflData.map(row => row("ID")).toSet mustEqual Set(2)

      val tags = dataHandler.select(AllColumns, NoCriterion, tagsTemplate)
      tags.map(row => row("TAG")).toSet mustEqual Set("lol", "rofl")
    }
  }
}
