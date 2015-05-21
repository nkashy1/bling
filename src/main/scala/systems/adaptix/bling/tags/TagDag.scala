package systems.adaptix.bling.tags

import systems.adaptix.bling.tags.graph.{RootedDag, DagVertex}
import scala.collection.mutable

/**
 * Created by nkashyap on 5/18/15.
 */
class TagDag(val universalTag: String) extends RootedDag(DagVertex(universalTag)) {
  val allTags = mutable.Set[String](universalTag)
  def hasTag(tag: String) = allTags contains tag
  def insertTag(tag: String) = {
    root addChild DagVertex(tag)
  }
}
