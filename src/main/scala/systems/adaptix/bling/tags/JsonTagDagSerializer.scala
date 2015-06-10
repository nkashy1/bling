package systems.adaptix.bling.tags

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.collection.mutable.Queue

import graph._

/**
 * Created by nkashyap on 6/10/15.
 */
class JsonTagDagSerializer extends TagDagSerializer {
  type SerializedTagDag = String

  case class TagDagSerializationInfo(universalTag: String, parentsAndChildren: Map[ String, Set[String] ])

  def serialize(tagDag: TagDag) = {
    val parentsAndChildren = Queue[ (String, Set[String]) ]()
    val itinerary = Queue[DagVertex](tagDag.root)
    var visited = Set[String]()
    while (itinerary.nonEmpty) {
      val currentVertex = itinerary.dequeue()
      visited = visited + currentVertex.label

      val childTags = Queue[String]()
      for (child <- currentVertex.children) {
        childTags.enqueue(child.label)
        if (!(visited contains child.label)) {
          itinerary.enqueue(child)
        }
      }

      parentsAndChildren.enqueue((currentVertex.label, childTags.toSet[String]))
    }

    val intermediary = TagDagSerializationInfo( tagDag.universalTag, parentsAndChildren.toMap[String, Set[String]] )
    val json =
      (("universalTag" -> intermediary.universalTag) ~ ("parentsAndChildren" -> intermediary.parentsAndChildren))
    compact(render(json))
  }

  def deserialize(serialization: SerializedTagDag) = {
    implicit val format = DefaultFormats
    val intermediary = parse(serialization).extract[TagDagSerializationInfo]
    val tagDag = TagDag(intermediary.universalTag)

    val itinerary = Queue[ TagDag#Tag ](tagDag.universalTag)
    var visited = Set[ TagDag#Tag ]()
    while (itinerary.nonEmpty) {
      val current = itinerary.dequeue()
      visited = visited + current
      for (tag <- intermediary.parentsAndChildren(current)) {
        if (tagDag.hasTag(tag)) {
          tagDag.link(current, tag)
        } else {
          tagDag.insertTag(tag, Set(current))
        }

        if (!(visited contains tag)) {
          itinerary.enqueue(tag)
        }
      }
    }
    tagDag
  }
}
