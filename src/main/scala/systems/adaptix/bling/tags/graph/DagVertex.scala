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
  def isParentOf(vertex: DagVertex): Boolean = {
    children contains vertex
  }
  def removeChild(child: DagVertex) = {
    children -= child
  }
}
