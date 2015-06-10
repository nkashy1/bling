package systems.adaptix.bling.tags

/**
 * Created by nkashyap on 6/9/15.
 */

trait TagDagSerializer {
  def serialize(tagDag: TagDag): String
  def deserialize(serialization: String): TagDag
}
