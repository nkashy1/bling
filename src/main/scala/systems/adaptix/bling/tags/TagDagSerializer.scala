package systems.adaptix.bling.tags

/**
 * Any serializer of TagDag intended for use in bling must extend this trait.
 *
 * Created by nkashyap on 6/9/15.
 */

trait TagDagSerializer {
  /**
   * @param tagDag
   * @return Serialized tagDag.
   */
  def serialize(tagDag: TagDag): String

  /**
   * @param serialization
   * @return The TagDag that was represented by the serialization.
   */
  def deserialize(serialization: String): TagDag
}
