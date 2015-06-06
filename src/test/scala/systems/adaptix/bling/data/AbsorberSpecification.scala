package systems.adaptix.bling.data

import org.specs2.mutable.Specification

/**
 * Created by nkashyap on 6/5/15.
 */
class AbsorberSpecification extends Specification {
  "The Absorber class allows TaggedData to be loaded into the table specified by a given TableTemplate in a database, with the tags being registered in a table specified by a TagTableTemplate." >> {
    val id = AutoIdFieldInfo("id")
    val name = PlainFieldInfo("name", "VARCHAR")
    val ts = NotNullFieldInfo("stamp", "TIMESTAMP")
    val dataTemplate = new TableTemplate("AbsorberSpecification_data", Seq(id, name, ts))

    val tag = PrimaryFieldInfo("tag", "VARCHAR")
    val tagsTemplate = new TagTableTemplate("AbsorberSpecification_tags", "tag")

    val absorber = new Absorber(dataTemplate, tagsTemplate)

    "The dataTemplate and tagsTemplate TableTemplates specifying the schema of the data table and the tags table respectively are passed at instantiation." >> {
      absorber.dataTemplate mustEqual dataTemplate
      absorber.tagsTemplate mustEqual tagsTemplate
    }

    "The absorb method." >> {
      pending
    }
  }
}
