package systems.adaptix.bling.tags

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.collection.mutable.Queue

import graph._

/**
 * A JSON serializer of TagDags.
 *
 * The format for the associated serialization is as follows:
 *
 * {
 *  "universalTag": <universalTag>,
 *  "parentsAndChildren": {
 *   <tag_1>: [<tag_1_child_1>, <tag_1_child_2>, ..., <tag_1_child_n1>],
 *   .
 *   .
 *   .
 *   <tag_m>: [<tag_m_child_1>, <tag_m_child_2>, ..., <tag_m_child_nm>]
 *  }
 * }
 *
 * Created by nkashyap on 6/10/15.
 */
trait JsonTagDagSerializer extends TagDagSerializer {
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

  def deserialize(serialization: String) = {
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
