package systems.adaptix.bling.data

/**
 * The TaggedData case class determines the general format in which data is accepted by bling. The actual data itself
 * is passed in as a Map[String, Any] and the tags are passed inside a Set[String].
 *
 * @param data An actual data point to be loaded into bling, passed as key-value pairs.
 * @param tags A set containing the tags that apply to the data point.
 * Created by nkashyap on 6/5/15.
 */
case class TaggedData(data: Map[String, Any], tags: Set[String])