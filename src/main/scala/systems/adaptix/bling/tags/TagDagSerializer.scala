package systems.adaptix.bling.tags

/**
 * Created by nkashyap on 6/9/15.
 */

trait TagDagSerializer {
  type SerializedTagDag
  def serialize(tagDag: TagDag): SerializedTagDag
  def deserialize(serialization: SerializedTagDag): TagDag
}
