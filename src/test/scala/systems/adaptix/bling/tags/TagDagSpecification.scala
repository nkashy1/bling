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
    "The labels of the vertices of a TagDag are stored as keys in its allTags member value, which is a mutable mapping from Strings to DagVertices. The value matching each of these tags is the DagVertex in the TagDag which has that tag as its label." >> {
      val tags = new TagDag("lol")
      tags.allTags must haveSize(1)
      tags.allTags.getOrElse("lol", "") mustEqual tags.root
    }

    "The \"hasTag\" method is a means of testing whether or not a TagDag already contains a vertex with a given tag." >> {
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
        tags.root.children must contain(tags allTags("new"))
      }

      "The method also allows the insertion of a tag as a child to a specified Set of parents." >> {
        val tags = new TagDag("root")
        tags.root.children must beEmpty
        tags.insertTag("child1")
        tags.insertTag("child2")
        tags.insertTag("grandchild", Set("child1", "child2"))

        val rootChildren = (tags allTags "root").children
        rootChildren must haveSize(2)
        rootChildren must contain(tags allTags "child1")
        rootChildren must contain(tags allTags "child2")
        rootChildren must not contain(tags allTags "grandchild")

        val child1Children = (tags allTags "child1").children
        child1Children must haveSize(1)
        child1Children must contain(tags allTags "grandchild")

        val child2Children = (tags allTags "child2").children
        child2Children must haveSize(1)
        child2Children must contain(tags allTags "grandchild")
      }
    }
  }
}
