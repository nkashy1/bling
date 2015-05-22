package systems.adaptix.bling.tags

import systems.adaptix.bling.tags.graph.{RootedDag, DagVertex}
import scala.collection.mutable

/**
 * Created by nkashyap on 5/18/15.
 */
class TagDag(val universalTag: String) extends RootedDag(DagVertex(universalTag)) {
  val tagVertices = mutable.Map[String, DagVertex](universalTag -> root)

  def hasTag(tag: String) = tagVertices.keys exists(_ == tag)

  def assertHasTag(tag: String) = {
    if ( !hasTag(tag) )
      throw new IllegalArgumentException("Tag does not exist: " + tag)
  }
  def assertHasNotTag(tag: String) = {
    if ( hasTag(tag) )
      throw new IllegalArgumentException("Tag already exists: " + tag)
  }

  def insertTag(tag: String, parents: Set[String] = Set(universalTag), children: Set[String] = Set()) = {
    assertHasNotTag(tag)
    val tagVertex = DagVertex(tag)
    parents map ( tagVertices(_) addChild tagVertex )
    tagVertex addChildren(
      children map { tagVertices(_) }
      )
    if (isAcyclic) {
      tagVertices += (tag -> tagVertex)
    } else {
      parents map ( tagVertices(_) removeChild tagVertex )
      throw new IllegalArgumentException("The attempted insertion did not preserve acyclicity: " + tag)
    }
  }

  def link(parentTag: String, childTag: String) = {
    assertHasTag(parentTag)
    assertHasTag(childTag)

    val parent = tagVertices(parentTag)
    val child = tagVertices(childTag)
    parent.addChild(child)
    if (!isAcyclic) {
      parent.removeChild(child)
      throw new IllegalArgumentException("Linkage led to acyclicity.")
    }
  }
}
