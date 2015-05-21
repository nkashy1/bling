package systems.adaptix.bling.tags

import systems.adaptix.bling.tags.graph.{RootedDag, DagVertex}
import scala.collection.mutable

/**
 * Created by nkashyap on 5/18/15.
 */
class TagDag(val universalTag: String) extends RootedDag(DagVertex(universalTag)) {
  val allTags = mutable.Map[String, DagVertex](universalTag -> root)

  def hasTag(tag: String) = allTags.keys exists(_ == tag)

  def insertTag(tag: String, parents: Set[String] = Set(universalTag)) = {
    if (hasTag(tag)) {
      throw new IllegalArgumentException("The attempted insertion did not preserve uniqueness of tags.")
    }
    val tagVertex = DagVertex(tag)
    parents map (allTags(_) addChild tagVertex)
    if (isAcyclic) {
      allTags += (tag -> tagVertex)
    } else {
      parents map (allTags(_) removeChild tagVertex)
      throw new IllegalArgumentException("The attempted insertion did not preserve acyclicity: " + tag)
    }
  }
}
