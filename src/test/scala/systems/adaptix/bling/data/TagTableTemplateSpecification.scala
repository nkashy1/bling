package systems.adaptix.bling.data

import org.specs2.mutable.Specification

/**
 * Created by nkashyap on 6/5/15.
 */
class TagTableTemplateSpecification extends Specification {
  "A TagTableTemplate is a TableTemplate specially designed to represent a column of tags." >> {
    val tagTableTemplate = new TagTableTemplate("lol", "rofl")

    "The single field in such a table is a primary key, meaning that there are no repetitions of tags." >> {
      tagTableTemplate.columns must haveSize(1)
      tagTableTemplate.columns.head must beAnInstanceOf[PrimaryFieldInfo]
    }
  }
}
