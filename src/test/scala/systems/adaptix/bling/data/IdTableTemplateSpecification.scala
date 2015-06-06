package systems.adaptix.bling.data

import org.specs2.mutable.Specification

/**
 * Created by nkashyap on 6/5/15.
 */
class IdTableTemplateSpecification extends Specification {
  "An IdTableTemplate represents a table which consists of a single column and which stores automatically generated IDs." >> {
    val idTableTemplate = new IdTableTemplate("lol", "rofl")

    "The single field in a table represented by an IdTableTemplate must be a primary key." >> {
      idTableTemplate.columns.head must beAnInstanceOf[PrimaryFieldInfo]
    }
  }
}
