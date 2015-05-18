package systems.adaptix.bling.tags.graph

import org.specs2.mutable.Specification

import scala.collection.mutable

/**
 * Created by nkashyap on 5/17/15.
 */
class DagVertexSpecification extends Specification {
  "A DagVertex is instantiated with a string which serves as its label." >> {
    val vertex = new DagVertex("test")
    vertex.label mustEqual "test"
  }

  "DagVertex instances are identified by reference." >> {
    val v1 = new DagVertex("lol")
    val v2 = new DagVertex("lol")
    val v3 = new DagVertex("rofl")

    v1 mustNotEqual v2
    v1 mustNotEqual v3
    v2 mustNotEqual v3
  }

  "A DagVertex instance has a \"children\" variable, which is of type mutable.Set, and is set to an empty set of DagVertices at instantiation." >> {
    val vertex = new DagVertex("test")
    vertex.children mustEqual mutable.Set[DagVertex]()
  }

  "Children may be added to a DagVertex using its \"addChild\" method." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    parent.addChild(child)
    parent.children must contain(child)
  }

  "The \"hasChild\" method of a DagVertex tests if a given DagVertex is one of its children." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    parent addChild child
    parent hasChild child must beTrue
    child hasChild parent must beFalse
  }

  "The \"removeChild\" method of a DagVertex removes a given vertex from its list of children if it was a child in the first place. Else it does nothing." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    val nonchild = new DagVertex("nonchild")
    parent addChild child
    parent hasChild child must beTrue
    parent hasChild nonchild must beFalse
    parent removeChild child
    parent hasChild child must beFalse
    parent removeChild nonchild
    parent hasChild nonchild must beFalse
  }

  "A DagVertex can be instantiated by calling the DagVertex singleton object." >> {
    val vertex = DagVertex("test")
    vertex.label mustEqual "test"
  }

  "The DagVertex singleton object also allows for the instantiation of a DagVertex with a given set of children." >> {
    val child1 = DagVertex("child1")
    val child2 = DagVertex("child2")
    val parent = DagVertex("parent", Set(child1, child2))
    parent.label mustEqual "parent"
    parent hasChild child1 must beTrue
    parent hasChild child2 must beTrue
    parent.children.size mustEqual 2
  }
}
