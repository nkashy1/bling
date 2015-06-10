package systems.adaptix.bling.tags

import scala.collection.mutable.Queue

import graph.DagVertex

/**
 * Created by nkashyap on 6/9/15.
 */

class VanillaTagDagSerializer(val entrySeparator: String = "|||", val parentChildrenSeparator: String = "~~", val childrenSeparator: String = ",") extends TagDagSerializer {
  type SerializedTagDag = String
  type Tag = String

  def serialize(tagDag: TagDag) = {
    var serialization: String = s"${tagDag.universalTag}${entrySeparator}"
    val itinerary = Queue[DagVertex](tagDag.root)
    var visited = Set[Tag]()
    while (!itinerary.isEmpty) {
      val current = itinerary.dequeue()
      serialization = serialization + s"${current.label}${parentChildrenSeparator}${current.children.mkString(childrenSeparator)}${entrySeparator}"
      visited = visited + current.label
      current.children.foreach( child => if (!(visited contains child.label)) {itinerary.enqueue(child)} )
    }
    serialization
  }

  def deserialize(serialization: SerializedTagDag): TagDag
}
