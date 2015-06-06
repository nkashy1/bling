package systems.adaptix.bling.data

import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll

import scalikejdbc._
import scalikejdbc.config._

/**
 * Created by nkashyap on 6/5/15.
 */
class AbsorberSpecification extends Specification with AfterAll {
  sequential

  DBs.setupAll()
  implicit val session = AutoSession

  val id = AutoIdFieldInfo("id")
  val name = PlainFieldInfo("name", "VARCHAR")
  val number = PlainFieldInfo("random", "INT")
  val dataTemplate = new TableTemplate("AbsorberSpecification_data", Seq(id, name, number))

  val tag = PrimaryFieldInfo("tag", "VARCHAR")
  val tagsTemplate = new TagTableTemplate("AbsorberSpecification_tags", "tag")

  val absorber = new Absorber(dataTemplate, tagsTemplate)

  def afterAll = {
    sql"${dataTemplate.sqlDrop}".execute.apply()
    sql"${tagsTemplate.sqlDrop}".execute.apply()
    absorber.tagIndexers.foreach(pair => sql"${pair._2.sqlDrop}".execute.apply())
  }

  "The Absorber class allows TaggedData to be loaded into the table specified by a given TableTemplate in a database, with the tags being registered in a table specified by a TagTableTemplate." >> {
    "The dataTemplate and tagsTemplate TableTemplates specifying the schema of the data table and the tags table respectively are passed at instantiation." >> {
      absorber.dataTemplate mustEqual dataTemplate
      absorber.tagsTemplate mustEqual tagsTemplate
    }

    "With a fresh database, the Absorber object's tagIndexers method should be empty." >> {
      absorber.tagIndexers must beEmpty
    }

    "The Absorber's functionality is exposed via the absorb method. It adds the data to the specified data table and adds the generated ID for the given data to the relevant tag indexers." >> {
      val input1 = TaggedData(Map[String, Any]("name" -> "bob"), Set("lol"))
      absorber.absorb(input1)
      absorber.tagIndexers.keySet mustEqual Set("lol")

      sql"SELECT id FROM ${absorber.tagIndexers("lol").sqlTableName}".map(_.toMap).first.apply() mustEqual Some(Map[String, Any]("ID" -> 1))

      var dataTableSelection = sql"SELECT * FROM ${absorber.dataTemplate.sqlTableName}".map(_.toMap).list.apply()
      dataTableSelection must haveSize(1)
      dataTableSelection(0) mustEqual Map[String, Any]("ID" -> 1, "NAME" -> "bob")

      val input2 = TaggedData(Map[String, Any]("name" -> "alice", "random" -> 4), Set("lol", "rofl"))
      absorber.absorb(input2)
      absorber.tagIndexers.keySet mustEqual Set("lol", "rofl")

      val lolSelection = sql"SELECT id FROM ${absorber.tagIndexers("lol").sqlTableName}".map(_.toMap).list.apply().toSet
      lolSelection must haveSize(2)
      lolSelection must contain(Map[String, Any]("ID" -> 1))
      lolSelection must contain(Map[String, Any]("ID" -> 2))

      val roflSelection = sql"SELECT id FROM ${absorber.tagIndexers("rofl").sqlTableName}".map(_.toMap).list.apply().toSet
      roflSelection must haveSize(1)
      roflSelection must contain(Map[String, Any]("ID" -> 2))

      dataTableSelection = sql"SELECT * FROM ${absorber.dataTemplate.sqlTableName}".map(_.toMap).list.apply()
      dataTableSelection must haveSize(2)
      dataTableSelection(0) mustEqual Map[String, Any]("ID" -> 1, "NAME" -> "bob")
      dataTableSelection(1) mustEqual Map[String, Any]("ID" -> 2, "NAME" -> "alice", "RANDOM" -> 4)

      val tagsTableSelection = sql"SELECT * FROM ${absorber.tagsTemplate.sqlTableName}".map(_.toMap).list.apply().toSet
      tagsTableSelection must haveSize(2)
      tagsTableSelection must contain(Map("TAG" -> "lol"))
      tagsTableSelection must contain(Map("TAG" -> "rofl"))
    }

    "validateFields is a utility method that can be used to check if a given TaggedData object is appropriate for absorption in terms of the fields that it inserts values into." >> {
      val validInput = TaggedData(Map[String, Any]("name" -> "bob", "random" -> 42), Set("lol"))
      absorber.validateFields(validInput) must beTrue

      val invalidInput = TaggedData(Map[String, Any]("lol" -> "rofl", "name" -> "divid"), Set("rofl"))
      absorber.validateFields(invalidInput) must beFalse
    }

    "The loadTagIndexers method allows the tagIndexers member variable to be updated to reflect independent updates to the database." >> {
      absorber.loadTagIndexers
      absorber.tagIndexers.keySet mustEqual Set("lol", "rofl")
    }
  }
}
