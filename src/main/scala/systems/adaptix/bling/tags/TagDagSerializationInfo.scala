package systems.adaptix.bling.tags

/**
 * TagDagSerializationInfo holds all the information which uniquely specifies a TagDag.
 *
 * Created by nkashyap on 6/10/15.
 */
case class TagDagSerializationInfo(universalTag: String, parentsAndChildren: Map[ String, Set[String] ])
