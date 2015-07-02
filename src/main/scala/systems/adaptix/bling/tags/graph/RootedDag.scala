package systems.adaptix.bling.tags.graph

import scala.collection.mutable.Stack

/**
 * A rooted, directed, acyclic graph implementation.
 * Created by nkashyap on 5/17/15.
 */
class RootedDag(val root: DagVertex) {
  /**
   * Checks whether the directed graph with the given root is acyclic.
   *
   * @return true if the graph is acyclic, false otherwise.
   */
  def isAcyclic: Boolean = {
    val components = stronglyConnectedComponents
    components.filter(_.size > 1).size == 0
  }

  /**
   * The stronglyConnectedComponents method returns a set consisting of sets, each containing the vertices in a strongly connected component
   * of the RootedDag. Each of the strongly connected components is guaranteed to be represented in this set.
   *
   * The method is a recursive implementation of Tarjan's algorithm:
   * 1. Robert Tarjan, Depth-first search and linear graph algorithms. Siam Journal of Computing, Vol. 1, No. 2, June 1972. Pages 146 -- 160
   * 2. http://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
   */
  def stronglyConnectedComponents: Set[Set[DagVertex]] = {
    var components = Set[Set[DagVertex]]()
    val S = Stack[DagVertex]()
    val trash = Stack[DagVertex]()
    var verticesVisited = 0

    def process(v: DagVertex): Int = {
      if (v.onStack) {
        v.index.get
      } else {
        S.push(v)
        v.onStack = true
        v.index = Some(verticesVisited)
        v.lowLink = Some(verticesVisited)
        verticesVisited += 1

        v.lowLink =
          Some(
            (v.children.map(process) + v.lowLink.get).min
          )

        if (v.index == v.lowLink) {
          var component = Set[DagVertex]()
          var member: DagVertex = null
          do {
            member = S.pop()
            member.onStack = false
            trash.push(member)
            component = component + member
          } while (member != v);

          components = components + component
        }

        v.lowLink.get
      }
    }

    process(root)

    while (trash.size > 0) {
      val vertex = trash.pop()
      vertex.index = None
      vertex.lowLink = None
    }

    components
  }
}

/**
 * Companion object to RootedDag. Allows creation of a RootedDag with simply the intended label of its root rather than the root vertex itself.
 */
object RootedDag {
  def apply(root: DagVertex) = new RootedDag(root)
  def apply(rootLabel: String) = new RootedDag(DagVertex(rootLabel))
}
