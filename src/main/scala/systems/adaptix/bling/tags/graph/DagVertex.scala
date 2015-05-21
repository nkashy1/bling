package systems.adaptix.bling.tags.graph

import scala.collection.mutable

/**
 * Created by nkashyap on 5/17/15.
 */
class DagVertex(val label: String) {
  val children = mutable.Set[DagVertex]()
  def addChild(child: DagVertex) = {
    children += child
  }
  def addChildren(children: Set[DagVertex]) = {
    children foreach addChild
  }
  def hasChild(vertex: DagVertex): Boolean = {
    children contains vertex
  }
  def hasChildren(vertices: Set[DagVertex]): Boolean ={
    !(vertices.map(children contains _) contains false)
  }
  def removeChild(child: DagVertex) = {
    children -= child
  }
  def removeChildren(children: Set[DagVertex]) = {
    children foreach removeChild
  }

  var onStack: Boolean = false
  var index: Option[Int] = None
  var lowLink: Option[Int] = None
}

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
