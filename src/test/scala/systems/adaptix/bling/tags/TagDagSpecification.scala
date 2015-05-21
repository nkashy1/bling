package systems.adaptix.bling.tags

import org.specs2.mutable.Specification
import scala.collection.mutable

/**
 * Created by nkashyap on 5/18/15.
 */
class TagDagSpecification extends Specification {
  "A TagDag is instantiated with a tag which represents universal context. This means that every data point is, if not explicitly then implicitly tagged with it." >> {
    val tags = new TagDag("lol")
    tags.universalTag mustEqual "lol"
  }

  "TagDag is a subclass of RootedDag, and hence a TagDag instance inherits a root. The universalTag of the TagDag is the label of this root vertex." >> {
    val tags = new TagDag("lol")
    tags.root.label mustEqual "lol"
  }

  "TagDags build upon RootedDags by providing a guarantee that each string occurs AT MOST ONCE as a vertex label. To this end, a TagDag provides an interface to vertex and label manipulation." >> {
    "The labels of the vertices of a TagDag are stored in its allTags member value, which is of type mutable.Set." >> {
      val tags = new TagDag("lol")
      tags.allTags must haveSize(1)
      tags.allTags must contain("lol")
    }

    "The \"hasTag\" method is a means of testing whether or not a TagDag already contains a given tag." >> {
      val tags = new TagDag("lol")
      tags hasTag("lol") must beTrue
      tags hasTag("rofl") must beFalse
    }

    "A new tag may be added to a TagDag via the \"insertTag\" method." >> {
      "By default, the tag is inserted as a child of the TagDag's root vertex." >> {
        val tags = new TagDag("root")
        tags.root.children must beEmpty
        tags.insertTag("new")
        tags.root.children must haveSize(1)
        tags.root.children.map(_.label == "new") mustEqual mutable.Set(true)
      }
    }
  }
}
