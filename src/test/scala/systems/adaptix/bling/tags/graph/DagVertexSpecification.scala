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

  "DagVertex objects are identified by reference." >> {
    val v1 = new DagVertex("lol")
    val v2 = new DagVertex("lol")
    val v3 = new DagVertex("rofl")

    v1 mustNotEqual v2
    v1 mustNotEqual v3
    v2 mustNotEqual v3
  }

  "A DagVertex object has a \"children\" variable, which is of type mutable.Set, and is set to an empty set of DagVertex objects at instantiation." >> {
    val vertex = new DagVertex("test")
    vertex.children mustEqual mutable.Set[DagVertex]()
  }

  "Children may be added to a DagVertex using its \"addChild\" method." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    parent.addChild(child)
    parent.children must contain(child)
  }

  "The \"isParentOf\" method of a DagVertex tests if a given DagVertex is one if its children." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    parent.addChild(child)
    parent.isParentOf(child) must beTrue
    child.isParentOf(parent) must beFalse
  }

  "The \"removeChild\" method of a DagVertex removes a given vertex from its list of children if it was a child in the first place. Else it does nothing." >> {
    val parent = new DagVertex("parent")
    val child = new DagVertex("child")
    val nonchild = new DagVertex("nonchild")
    parent.addChild(child)
    parent.isParentOf(child) must beTrue
    parent.isParentOf(nonchild) must beFalse
    parent.removeChild(child)
    parent.isParentOf(child) must beFalse
    parent.removeChild(nonchild)
    parent.isParentOf(nonchild) must beFalse
  }
}
