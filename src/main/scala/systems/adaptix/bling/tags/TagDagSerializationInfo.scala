package systems.adaptix.bling.tags

/**
 * Created by nkashyap on 6/10/15.
 */
case class TagDagSerializationInfo(universalTag: String, parentsAndChildren: Map[ String, Set[String] ])
