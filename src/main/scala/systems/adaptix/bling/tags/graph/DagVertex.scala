package systems.adaptix.bling.tags.graph

import scala.collection.mutable

/**
 * bling's representation of a vertex in an acyclic, directed graph.
 *
 * Created by nkashyap on 5/17/15.
 */
class DagVertex(val label: String) {
  val children = mutable.Set[DagVertex]()

  /**
   * Adds a child to the DagVertex's list of children.
   * @param child
   * @return
   */
  def addChild(child: DagVertex) = {
    children += child
  }

  /**
   * Adds every DagVertex in the given Set of DagVertices to the DagVertex's list of children.
   *
   * @param children
   */
  def addChildren(children: Set[DagVertex]) = {
    children foreach addChild
  }

  /**
   * Checks if the DagVertex has the given DagVertex as a child.
   *
   * @param vertex
   * @return true if vertex is a child of the DagVertex, false otherwise.
   */
  def hasChild(vertex: DagVertex): Boolean = {
    children contains vertex
  }

  /**
   * Checks if the DagVertex has ALL of the DagVertices in the given Set as children.
   *
   * @param vertices
   * @return true if every vertex in vertices is a child of the DagVertex, false otherwise.
   */
  def hasChildren(vertices: Set[DagVertex]): Boolean ={
    !(vertices.map(children contains _) contains false)
  }

  /**
   * Removes the specified child from the list of children of the DagVertex.
   *
   * @param child
   * @return
   */
  def removeChild(child: DagVertex) = {
    children -= child
  }

  /**
   * Removes each of the specified children from the list of children of the DagVertex.
   *
   * @param children
   */
  def removeChildren(children: Set[DagVertex]) = {
    children foreach removeChild
  }

  var onStack: Boolean = false
  var index: Option[Int] = None
  var lowLink: Option[Int] = None
}

/**
 * Companion object to DagVertex. Allows creation of a DagVertex with a specified set of children.
 */
object DagVertex {
  def apply (label: String) = {
    new DagVertex(label)
  }
  def apply(label: String, children: Set[DagVertex]) = {
    val vertex = new DagVertex(label)
    children foreach vertex.addChild
    vertex
  }
}
