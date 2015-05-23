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
    "The labels of the vertices of a TagDag are stored as keys in its tagVertices member value, which is a mutable mapping from Strings to DagVertices. The value matching each of these tags is the DagVertex in the TagDag which has that tag as its label." >> {
      val tags = new TagDag("lol")
      tags.tagVertices must haveSize(1)
      tags.tagVertices.getOrElse("lol", "") mustEqual tags.root
    }

    "The \"hasTag\" method is a means of testing whether or not a TagDag already contains a vertex with a given tag." >> {
      val tags = new TagDag("lol")
      tags hasTag "lol" must beTrue
      tags hasTag "rofl" must beFalse
    }

    "It is possible to make assertions regarding the existence of tags." >> {
      "\"assertHasTag\" throws an IllegalArgumentException if its argument IS NOT a registered tag." >> {
        val tags = new TagDag("root")
        tags.assertHasTag("root") mustEqual ()
        tags.assertHasTag("lol") must throwA[IllegalArgumentException]
      }

      "\"assertHasNotTag\" throws an IllegalArgumentException if its argument IS a registered tag." >> {
        val tags = new TagDag("root")
        tags.assertHasNotTag("root") must throwA[IllegalArgumentException]
        tags.assertHasNotTag("lol") mustEqual ()
      }
    }

    "A new tag may be added to a TagDag via the \"insertTag\" method." >> {
      "By default, the tag is inserted as a child of the TagDag's root vertex." >> {
        val tags = new TagDag("root")
        tags.root.children must beEmpty
        tags.insertTag("new")
        tags.root.children must haveSize(1)
        tags.root.children must contain(tags tagVertices("new"))
      }

      "The method also allows the insertion of a tag as a child to a specified Set of parents." >> {
        val tags = new TagDag("root")
        tags.insertTag("child1")
        tags.insertTag("child2")
        tags.insertTag("grandchild", Set("child1", "child2"))

        val rootChildren = (tags tagVertices "root").children
        rootChildren must haveSize(2)
        rootChildren must contain(tags tagVertices "child1")
        rootChildren must contain(tags tagVertices "child2")
        rootChildren must not contain(tags tagVertices "grandchild")

        val child1Children = (tags tagVertices "child1").children
        child1Children must haveSize(1)
        child1Children must contain(tags tagVertices "grandchild")

        val child2Children = (tags tagVertices "child2").children
        child2Children must haveSize(1)
        child2Children must contain(tags tagVertices "grandchild")
      }

      "The method also allows for specification of a Set of children of the tag to be inserted." >> {
        val tags = new TagDag("root")
        tags.insertTag("child1")
        tags.insertTag("child2", parents = Set("root"), children = Set("child1"))

        val rootChildren = (tags tagVertices "root").children
        rootChildren must haveSize(2)
        rootChildren must contain(tags tagVertices "child1")
        rootChildren must contain(tags tagVertices "child2")

        val child1Children = (tags tagVertices "child1").children
        child1Children must beEmpty

        val child2Children = (tags tagVertices "child2").children
        child2Children must haveSize(1)
        child2Children must contain(tags tagVertices "child1")
      }

      "Any insertion which ultimately violates acyclicity will result in an IllegalArgumentException and a reversion of state of the TagDag to before the insertion was attempted." >> {
        val tags = new TagDag("root")
        tags.insertTag("child1")
        tags.insertTag("child2", children = Set("root")) must throwA[IllegalArgumentException]
        tags.tagVertices must haveSize(2)
        val root = tags tagVertices "root"
        root.children must haveSize(1)
        root.children must contain(tags tagVertices "child1")
      }

      "The attempted insertion of a tag which already exists will cause the method to throw an IllegalArgumentException." >> {
        val tags = new TagDag("root")
        tags.insertTag("root") must throwA[IllegalArgumentException]
      }

      "The method also throws an IllegalArgumentException if one of the specified parents or children is not a registered tag." >> {
        val tags = new TagDag("root")
        tags.insertTag("lol", Set("fakeTag")) must throwA[IllegalArgumentException]
        tags.insertTag("lol", children = Set("fakeTag")) must throwA[IllegalArgumentException]
      }
    }

    "The \"descendants\" method returns the tags which are reachable from the given tag." >> {
      "The tags are returned in a container of type Seq[String]." >> {
        val tags = new TagDag("root")
        tags.insertTag("lol")
        tags.insertTag("rofl")
        tags.insertTag("omg")
        tags.insertTag("wtf", Set("omg"))
        tags.insertTag("bbq", Set("wtf"))

        val descendantsOfOmg = tags descendants "omg"
        descendantsOfOmg must haveSize(3)
        descendantsOfOmg must contain("omg")
        descendantsOfOmg must contain("wtf")
        descendantsOfOmg must contain("bbq")

        val descendantsOfRoot = tags descendants "root"
        descendantsOfRoot must haveSize(6)
        descendantsOfRoot(0) mustEqual "root"
        descendantsOfRoot must contain("lol")
        descendantsOfRoot must contain("rofl")
        descendantsOfRoot must contain("omg")
        descendantsOfRoot(4) mustEqual "wtf"
        descendantsOfRoot(5) mustEqual "bbq"
      }

      "The method throws an IllegalArgumentException if the originating tag is not registered in the TagDag." >> {
        val tags = new TagDag("root")
        (tags descendants "fakeTag") must throwA[IllegalArgumentException]
      }
    }

    "The \"validateUniversality\" method checks if every registered tag in a TagDag is reachable from the universalTag." >> {
      val tags = new TagDag("root")
      tags insertTag "child"
      tags.validateUniversality must beTrue

      tags.root removeChild tags.tagVertices("child")
      tags.validateUniversality must beFalse
    }

    "The \"link\" method facilitates edge creation." >> {
      "The argument order determines the direction of the edge." >> {
        val tags = new TagDag("root")
        tags.insertTag("parent")
        tags.insertTag("child")
        tags.link("parent", "child")
        val parent = tags.tagVertices("parent")
        val child = tags.tagVertices("child")
        tags.root hasChildren Set(parent, child) must beTrue
        parent.children must haveSize(1)
        parent hasChild child must beTrue
        child.children must beEmpty
      }

      "The method throws an exception if either one of the tags has not been registered in the TagDag." >> {
        val tags = new TagDag("root")
        tags.insertTag("vertex")
        tags.link("fakeParent", "vertex") must throwA[IllegalArgumentException]
        tags.link("vertex", "fakeChild") must throwA[IllegalArgumentException]
      }

      "The method reverts the TagDag to its previous state and throws an IllegalArgumentException if the introduction of the link creates a cycle in the TagDag." >> {
        val tags = new TagDag("root")
        tags.insertTag("child")
        val child = tags tagVertices "child"

        tags.link("child", "root") must throwA[IllegalArgumentException]
        tags.root.children must haveSize(1)
        tags.root.children must contain(child)
        child.children must beEmpty
      }
    }

    "The \"unlink\" method facilitates edge removal." >> {
      "The argument order determines the direction of the edge." >> {
        val tags = new TagDag("root")
        tags.insertTag("child")
        tags.insertTag("sibling", Set("root", "child"))
        tags.tagVertices("root").children must contain(tags.tagVertices("sibling"))
        tags.tagVertices("child").children must contain(tags.tagVertices("sibling"))

        tags.unlink("child", "sibling")
        tags.tagVertices("root").children must contain(tags.tagVertices("sibling"))
        tags.tagVertices("child").children must not contain(tags.tagVertices("sibling"))
      }

      "The method throws an IllegalArgumentException if either of its arguments has not been registered in the TagDag." >> {
        val tags = new TagDag("root")
        tags.unlink("root", "fakeTag") must throwA[IllegalArgumentException]
        tags.unlink("fakeTag", "root") must throwA[IllegalArgumentException]
      }

      "The method gracefully does nothing if, although both its arguments are registered in the TagDag, there is no edge between them in the specified direction." >> {
        val tags = new TagDag("root")
        tags.insertTag("child")
        tags.root.children must haveSize(1)
        tags.root.children must contain(tags.tagVertices("child"))
      }

      "The method reverts the TagDag to its previous state and throws an IllegalArgumentException if the specified unlinking causes the child node to become isolated." >> {
        val tags = new TagDag("root")
        tags.insertTag("child")
        tags.unlink("root", "child") must throwA[IllegalArgumentException]
        tags.root.children must contain(tags.tagVertices("child"))
      }
    }

    "The \"groupSiblings\" method facilitates the creation of a new tag representing the aggregation of some subset of the children of a given contextual tag." >> {
      "The new tag is interjected between the contextual tag and its children." >> {
        val tags = new TagDag("context")
        tags.insertTag("member1")
        tags.insertTag("member2")
        tags.insertTag("non-member")

        tags.groupSiblings("group", Set("member1", "member2"), "context")

        Set("member1", "member2", "context").
          map( tag => tags.tagVertices(tag) ).
          exists( vertex => tags.tagVertices("context").hasChild(vertex) ) must beFalse
        Set("group", "non-member").
          map( tag => tags.tagVertices(tag) ).
          forall( vertex => tags.tagVertices("context").hasChild(vertex) ) must beTrue

        Set("member1", "member2").
          map( tag => tags.tagVertices(tag) ).
          forall( vertex => tags.tagVertices("group").hasChild(vertex) ) must beTrue
        Set("context", "non-member", "group").
          map( tag => tags.tagVertices(tag) ).
          exists( vertex => tags.tagVertices("group").hasChild(vertex) ) must beFalse

        tags.tagVertices("context").children must haveSize(2)
        tags.tagVertices("group").children must haveSize(2)
      }

      "If the groupTag has already been registered in the TagDag, the method throws an IllegalArgumentException." >> {
        val tags = new TagDag("context")
        tags.insertTag("member")

        tags.groupSiblings("member", Set("member"), "context") must throwA[IllegalArgumentException]
      }

      "If the contextTag or one of the memberTags is NOT a registered tag, or if any of the memberTags is not a child of the contextTag, the method throws an IllegalArgumentException." >> {
        val tags = new TagDag("context")
        tags.insertTag("shallow-member")
        tags.insertTag("deep-member", Set("shallow-member"))

        tags.groupSiblings("lol", Set("shallow-member"), "fakeTag") must throwA[IllegalArgumentException]("The contextTag and all the memberTags have to be registered.")
        tags.groupSiblings("lol", Set("fakeTag"), "context") must throwA[IllegalArgumentException]("The contextTag and all the memberTags have to be registered.")
        tags.groupSiblings("lol", Set("deep-member"), "context") must throwA[IllegalArgumentException]("memberTags have to be children of the contextTag.")
      }
    }
  }
}
