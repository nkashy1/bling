package systems.adaptix.bling.tags

import java.util.NoSuchElementException

import graph.{RootedDag, DagVertex}
import scala.collection.mutable

/**
 * A TagDag is an acyclic, rooted, directed graph in which every vertex is labelled by a Tag, which is simply a String.
 *
 * The universalTag is the Tag corresponding to the root of the TagDag.
 *
 * Created by nkashyap on 5/18/15.
 */
class TagDag(val universalTag: String) extends RootedDag(DagVertex(universalTag)) {
  type Tag = String

  val tagVertices = mutable.Map[Tag, DagVertex](universalTag -> root)

  /**
   * Checks whether the given Tag has previously been registered in the TagDag.
   *
   * @param tag
   * @return true if the Tag was previously registered, false otherwise.
   */
  def hasTag(tag: Tag) = tagVertices.keys exists(_ == tag)

  /**
   * Inserts a vertex labelled with the given Tag into the TagDag.
   *
   * @param tag The tag to be inserted.
   * @param parents The tags which are to be linked to the new tag as parents.
   * @param children The tags which are to be linked to the new tag as children.
   * @return
   */
  def insertTag(tag: Tag, parents: Set[Tag] = Set(universalTag), children: Set[Tag] = Set()) = {
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
  def groupSiblings(groupTag: Tag, memberTags: Set[Tag], contextTag: Tag = universalTag) = {
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

  /**
   * Pushes a newly inserted tag (which is a child of root) down the TagDag so that it becomes child to a more suitable parent.
   * Note that this method is intended to be applied to newly inserted tags only. As such, no check for acyclicity or universality
   * is made as a side-effect of the method call.
   *
   * @param target
   * @param newParent
   */
  def pushChild(target: Tag, newParent: Tag) = {
    assertHasTag(target)
    assertHasTag(newParent)

    val targetVertex = tagVertices(target)
    val parentVertex = tagVertices(newParent)
    parentVertex addChild targetVertex
    root removeChild targetVertex
  }

  /**
   * Creates an directed edge from parentTag to childTag as long as this does not make the graph cyclic.
   *
   * @param parentTag
   * @param childTag
   */
  def link(parentTag: Tag, childTag: Tag) = {
    assertHasTag(parentTag)
    assertHasTag(childTag)

    val parent = tagVertices(parentTag)
    val child = tagVertices(childTag)
    parent.addChild(child)
    if (!isAcyclic) {
      parent.removeChild(child)
      throw new IllegalArgumentException("Linking tags violated acyclicity: " + parentTag + " to " + childTag + ".")
    }
  }

  /**
   * Deletes an edge from parentTag to childTag as long as this does not disconnect make the child inaccessible from the root.
   *
   * @param parentTag
   * @param childTag
   */
  def unlink(parentTag: Tag, childTag: Tag) = {
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

  /**
   * Checks whether every vertex of the TagDag is accessible from its root.
   *
   * @return true if every registered Tag is a descendant of root, false otherwise.
   */
  def validateUniversality = {
    val descendantsOfRoot = descendants(universalTag)
    tagVertices.keys.forall( descendantsOfRoot contains _ )
  }

  /**
   * Returns a sequence containing the tags descendant from the given tag in order of breadth-first trarversal.
   *
   * @param tag
   * @return Seq[Tag]
   */
  def descendants(tag: Tag) = {
    assertHasTag(tag)

    var descendantTags = Seq[Tag]()
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
  /**
   * Throws exception if given Tag IS NOT registered in the TagDag.
   *
   * @param tag
   */
  def assertHasTag(tag: Tag) = {
    if ( !hasTag(tag) )
      throw new IllegalArgumentException("Tag does not exist: " + tag)
  }

  /**
   * Throws exception if given Tag IS registered in the TagDag.
   * @param tag
   */
  def assertHasNotTag(tag: Tag) = {
    if ( hasTag(tag) )
      throw new IllegalArgumentException("Tag already exists: " + tag)
  }
}

/**
 * Companion object to TagDag class.
 */
object TagDag {
  def apply(universalTag: String) = new TagDag(universalTag)
}
