package systems.adaptix.bling.data

import org.joda.time.DateTime
import org.specs2.mutable.Specification

import scalikejdbc._
import scalikejdbc.config._

/**
 * Created by nkashyap on 6/5/15.
 */
class AbsorberSpecification extends Specification {
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

  "The Absorber class allows TaggedData to be loaded into the table specified by a given TableTemplate in a database, with the tags being registered in a table specified by a TagTableTemplate." >> {
    "The dataTemplate and tagsTemplate TableTemplates specifying the schema of the data table and the tags table respectively are passed at instantiation." >> {
      absorber.dataTemplate mustEqual dataTemplate
      absorber.tagsTemplate mustEqual tagsTemplate
    }

    "With a fresh database, the Absorber object's tagIndexers method should be empty." >> {
      absorber.tagIndexers must beEmpty
    }

    "The Absorber's functionality is exposed via the absorb method." >> {
      val input1 = TaggedData(Map[String, Any]("name" -> "bob"), Set("lol"))
      absorber.absorb(input1)
      absorber.tagIndexers.keySet must contain("lol")
      sql"SELECT id FROM ${absorber.tagIndexers("lol").sqlTableName}".map(_.toMap).first.apply() mustEqual Some(Map[String, Any]("id".toUpperCase -> 1))
    }

    "validateFields is a utility method that can be used to check if a given TaggedData object is appropriate for absorption in terms of the fields that it inserts values into." >> {
      val validInput = TaggedData(Map[String, Any]("name" -> "bob", "random" -> 42), Set("lol"))
      absorber.validateFields(validInput) must beTrue

      val invalidInput = TaggedData(Map[String, Any]("lol" -> "rofl", "name" -> "divid"), Set("rofl"))
      absorber.validateFields(invalidInput) must beFalse
    }
  }

  "Cleaning up." >> {
    sql"${dataTemplate.sqlDrop}".execute.apply()
    sql"${tagsTemplate.sqlDrop}".execute.apply()
    absorber.tagIndexers.foreach(pair => sql"${pair._2.sqlDrop}".execute.apply())
    ok
  }
}
