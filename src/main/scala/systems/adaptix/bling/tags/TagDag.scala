package systems.adaptix.bling.tags

import java.util.NoSuchElementException

import systems.adaptix.bling.tags.graph.{RootedDag, DagVertex}
import scala.collection.mutable

/**
 * Created by nkashyap on 5/18/15.
 */
class TagDag(val universalTag: String) extends RootedDag(DagVertex(universalTag)) {
  val tagVertices = mutable.Map[String, DagVertex](universalTag -> root)

  def hasTag(tag: String) = tagVertices.keys exists(_ == tag)

  def insertTag(tag: String, parents: Set[String] = Set(universalTag), children: Set[String] = Set()) = {
    assertHasNotTag(tag)
    parents foreach { assertHasTag }
    children foreach { assertHasTag }

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

  /**
   * Creates a group tag to denote a collection of children of a common parent tag. The group tag is added inbetween the parent tag
   * and the group member tags. The edges from the parent to each of the group members are erased.
   *
   * Note that the groupTag cannot be a previously registered tag. Moreover, the grouping operation local to the parent tag. Therefore, no
   * cycle is created as a side-effect of this method.
   *
   * @param groupTag
   * @param memberTags
   * @param contextTag
   */
  def groupSiblings(groupTag: String, memberTags: Set[String], contextTag: String = universalTag) = {
    assertHasNotTag(groupTag)
    try{
      if (memberTags exists { member => !tagVertices(contextTag).hasChild(tagVertices(member)) }) {
        throw new IllegalArgumentException("memberTags have to be children of the contextTag.")
      }
    } catch {
      case ex: NoSuchElementException => throw new IllegalArgumentException("The contextTag and all the memberTags have to be registered.")
    }

    insertTag(groupTag, Set(contextTag), memberTags)
    tagVertices(contextTag).removeChildren(
      memberTags map { tagVertices }
    )
  }

  def link(parentTag: String, childTag: String) = {
    assertHasTag(parentTag)
    assertHasTag(childTag)

    val parent = tagVertices(parentTag)
    val child = tagVertices(childTag)
    parent.addChild(child)
    if (!isAcyclic) {
      parent.removeChild(child)
      throw new IllegalArgumentException("Linking tags violated acyclicity.")
    }
  }

  def unlink(parentTag: String, childTag: String) = {
    assertHasTag(parentTag)
    assertHasTag(childTag)

    val parent = tagVertices(parentTag)
    val child = tagVertices(childTag)
    parent.removeChild(child)

    // TODO: This is clearly not the optimal way to check universality after an edge deletion. Just check if you can still get from root to child of deleted edge.
    if (!validateUniversality) {
      parent.addChild(child)
      throw new IllegalArgumentException("Unlinking tags violated universality.")
    }
  }

  def validateUniversality = {
    val descendantsOfRoot = descendants(universalTag)
    tagVertices.keys.forall( descendantsOfRoot contains _ )
  }

  /**
   * Returns a sequence containing the tags descendant from the given tag in order of breadth-first trarversal.
   * @param tag
   * @return Seq[String]
   */
  def descendants(tag: String) = {
    assertHasTag(tag)

    var descendantTags = Seq[String]()
    val toVisit = mutable.Queue[DagVertex](tagVertices(tag))

    def process(vertex: DagVertex) = {
      descendantTags = descendantTags :+ vertex.label
      vertex.children foreach { toVisit.enqueue(_) }
    }

    while (!toVisit.isEmpty) {
      process(toVisit.dequeue)
    }

    descendantTags
  }

  // TODO: Get rid of assertHasTag. Would be better to handle with a try-catch statement. assertHasNotTag is still useful.
  def assertHasTag(tag: String) = {
    if ( !hasTag(tag) )
      throw new IllegalArgumentException("Tag does not exist: " + tag)
  }
  def assertHasNotTag(tag: String) = {
    if ( hasTag(tag) )
      throw new IllegalArgumentException("Tag already exists: " + tag)
  }
}
