package systems.adaptix.bling.tags.graph

import org.specs2.mutable.Specification

/**
 * Created by nkashyap on 5/17/15.
 */
class RootedDagSpecification extends Specification {
  "A RootedDag is instantiated with a DagVertex which is its distinguished root." >> {
    val root = DagVertex("root")
    val rootedDag = new RootedDag(root)
    rootedDag.root mustEqual root
  }

  "The RootedDag \"stronglyConnectedComponents\" method returns a set containing the strongly connected components of the RootedDag instance." >> {
    "The strongly connected components are represented by the set of their vertices." >> {
      val root = DagVertex("root")
      val rootedDag = new RootedDag(root)
      val components = rootedDag.stronglyConnectedComponents
      components must haveSize(1)
      components map (_.contains(root)) mustEqual Set(true)
    }

    "After the method call, each vertex in the RootedDag should have its state refreshed so that its index and lowLink parameters are once again None." >> {
      val child = DagVertex("child")
      val root = DagVertex("root", Set(child))
      val rootedDag = new RootedDag(root)
      val components = rootedDag.stronglyConnectedComponents
      root.index must beNone
      root.lowLink must beNone
      child.index must beNone
      child.lowLink must beNone
    }

    "Example 1: Root and one child. Two strongly connected components." >> {
      val child = DagVertex("child")
      val root = DagVertex("root", Set(child))
      val rootedDag = new RootedDag(root)
      val components = rootedDag.stronglyConnectedComponents
      components must haveSize(2)
    }

    "Example 2: Root and one child with a back link from the child to the root. One strongly connected component." >> {
      val child = DagVertex("child")
      val root = DagVertex("root", Set(child))
      child addChild root
      val rootedDag = new RootedDag(root)
      val components = rootedDag.stronglyConnectedComponents
      components must haveSize(1)
    }

    "Example 3: Vertices A, B, C, D, E. A is roote with children B and C. B has D and E as children. Five strongly connected components." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(D, E)
      val rootedDag = new RootedDag(A)
      val components = rootedDag.stronglyConnectedComponents
      components must haveSize(5)
    }

    "Example 4: Vertices A, B, C, D, E. A is root, with children B and C. B has D and E as children. D connects back to A. Three strongly connected components." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(D, E)
      D addChild A
      val rootedDag = new RootedDag(A)
      val components = rootedDag.stronglyConnectedComponents
      components must haveSize(3)
    }

    "Example 5: Vertices A, B, C, D, E. A is root with children B and C, B has D and E as children. D has C as a child. C has A as a child. Two strongly connected components." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(D, E)
      D addChild C
      C addChild A
      val rootedDag = new RootedDag(A)
      val components = rootedDag.stronglyConnectedComponents
      components must haveSize(2)
    }

    "Example 6: Vertices A, B, C, D, E. A is root. A has B and C as children. B has C and D as children. C has D and E as children. D has C and B as children. Three strongly connected components." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(C, D)
      C addChildren Set(D, E)
      D addChildren Set(B, C)
      val rootedDag = new RootedDag(A)
      val components = rootedDag.stronglyConnectedComponents
      components must haveSize(3)
    }

    "Example 7: Vertices A-G. A is root with B and F as children. B as C and D as children. C has E as a child. D has C and E as children. E has D as a child. F has G as a child. G has A as a child. Three strongly connected components." >> {
      val A = DagVertex("")
      val B = DagVertex("")
      val C = DagVertex("")
      val D = DagVertex("")
      val E = DagVertex("")
      val F = DagVertex("")
      val G = DagVertex("")
      A addChildren Set(B, F)
      B addChildren Set(C, D)
      C addChild E
      D addChildren Set(C, E)
      E addChild D
      F addChild G
      G addChild A
      val rootedDag = new RootedDag(A)
      rootedDag.stronglyConnectedComponents must haveSize(3)
    }
  }

  "The \"isAcyclic\" method tests whether the graph is indeed acyclic." >> {
    "Example 0: Only one vertex. Acyclic." >> {
      val root = DagVertex("")
      val rootedDag = new RootedDag(root)
      rootedDag.isAcyclic must beTrue
    }

    "Example 1: Root and one child. Acyclic." >> {
      val child = DagVertex("child")
      val root = DagVertex("root", Set(child))
      val rootedDag = new RootedDag(root)
      rootedDag.isAcyclic must beTrue
    }

    "Example 2: Root and one child with a back link from the child to the root. Cyclic." >> {
      val child = DagVertex("child")
      val root = DagVertex("root", Set(child))
      child addChild root
      val rootedDag = new RootedDag(root)
      rootedDag.isAcyclic must beFalse
    }

    "Example 3: Vertices A, B, C, D, E. A is root, with children B and C. B has D and E as children. Acyclic." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(D, E)
      val rootedDag = new RootedDag(A)
      rootedDag.isAcyclic must beTrue
    }

    "Example 4: Vertices A, B, C, D, E. A is root, with children B and C. B has D and E as children. D connects back to A. Cyclic." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(D, E)
      D addChild A
      val rootedDag = new RootedDag(A)
      rootedDag.isAcyclic must beFalse
    }

    "Example 5: Vertices A, B, C, D, E. A is root with children B and C, B has D and E as children. D has C as a child. C has A as a child. Cyclic." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(D, E)
      D addChild C
      C addChild A
      val rootedDag = new RootedDag(A)
      rootedDag.isAcyclic must beFalse
    }

    "Example 6: Vertices A, B, C, D, E. A has B and C as children. B has C and D as children. C has D and E as children. D has C and B as children. Cyclic." >> {
      val A = DagVertex("A")
      val B = DagVertex("B")
      val C = DagVertex("C")
      val D = DagVertex("D")
      val E = DagVertex("E")
      A addChildren Set(B, C)
      B addChildren Set(C, D)
      C addChildren Set(D, E)
      D addChildren Set(B, C)
      val rootedDag = new RootedDag(A)
      rootedDag.isAcyclic must beFalse
    }

    "Example 7: Vertices A-G. A is root with B and F as children. B as C and D as children. C has E as a child. D has C and E as children. E has D as a child. F has G as a child. G has A as a child. Cyclic." >> {
      val A = DagVertex("")
      val B = DagVertex("")
      val C = DagVertex("")
      val D = DagVertex("")
      val E = DagVertex("")
      val F = DagVertex("")
      val G = DagVertex("")
      A addChildren Set(B, F)
      B addChildren Set(C, D)
      C addChild E
      D addChildren Set(C, E)
      E addChild D
      F addChild G
      G addChild A
      val rootedDag = new RootedDag(A)
      rootedDag.isAcyclic must beFalse
    }

    "Example 8: Root, two children, and a grandchild common to the two children. Acyclic." >> {
      val root = DagVertex("")
      val child1 = DagVertex("")
      val child2 = DagVertex("")
      val grandchild = DagVertex("")
      root addChildren Set(child1, child2)
      child1 addChild grandchild
      child2 addChild grandchild
      val rootedDag = new RootedDag(root)
      rootedDag.isAcyclic must beTrue
    }
  }

  "The RootedDag companion object may be used to instantiate a new RootedDag." >> {
    "It allows instantiation without the use of \"new\"." >> {
      val root = DagVertex("")
      val rootedDag = RootedDag(root)
      rootedDag.root mustEqual root
    }

    "It also allows instantiation of a RootedDag with root having a specified label." >> {
      val rootedDag = RootedDag("lol")
      rootedDag.root.label mustEqual "lol"
    }
  }
}
