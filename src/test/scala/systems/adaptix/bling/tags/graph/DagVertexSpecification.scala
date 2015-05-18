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

  "Children may be added individually to a DagVertex using its \"addChild\" method." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    parent.addChild(child)
    parent.children must haveSize(1)
    parent.children must contain(child)
  }

  "A set of vertices may be passed to the \"addChildren\" method to perform a batch addition." >> {
    val parent = new DagVertex("parent")
    val child1 = new DagVertex("child1")
    val child2 = new DagVertex("child2")
    parent addChildren Set(child1, child2)
    parent.children must haveSize(2)
    parent.children must contain(child1)
    parent.children must contain(child2)
  }

  "The \"hasChild\" method of a DagVertex tests if a given DagVertex is one of its children." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    parent addChild child
    parent hasChild child must beTrue
    child hasChild parent must beFalse
  }

  "The \"hasChildren\" method of a DagVertex tests if all the vertices in a given set are its children." >> {
    val parent = new DagVertex("")
    val child1 = new DagVertex("")
    val child2 = new DagVertex("")
    val nonchild = new DagVertex("")
    parent addChildren Set(child1, child2)
    parent.hasChildren(Set(child1, child2)) must beTrue
    parent.hasChildren(Set(nonchild)) must beFalse
    parent.hasChildren(Set(child1, nonchild)) must beFalse
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

  "The \"removeChildren\" method performs a batch deletion of children present in a given set of vertices." >> {
    val parent = new DagVertex("")
    val child1 = new DagVertex("")
    val child2 = new DagVertex("")
    val nonchild = new DagVertex("")
    parent addChildren Set(child1, child2)
    parent removeChildren Set(child1,  nonchild)
    parent.children must haveSize(1)
    parent.children must contain(child2)
  }

  "A DagVertex also has the member variables \"index\" and \"backLink\", both of type Option[Int] which are used to validate acyclicity. They are set to None at instantiation." >> {
    val test = new DagVertex("test")
    test.index must beNone
    test.lowLink must beNone
  }

  "A DagVertex can be instantiated by calling the DagVertex companion object." >> {
    val vertex = DagVertex("test")
    vertex.label mustEqual "test"
  }

  "The DagVertex companion object also allows for the instantiation of a DagVertex with a given set of children." >> {
    val child1 = DagVertex("child1")
    val child2 = DagVertex("child2")
    val parent = DagVertex("parent", Set(child1, child2))
    parent.label mustEqual "parent"
    parent hasChild child1 must beTrue
    parent hasChild child2 must beTrue
    parent.children.size mustEqual 2
  }
}
