package systems.adaptix.bling

import data._
import tags._

import java.io._
import scala.io.Source

/**
 * BlingConsole represents the external interface to bling. It cannot be instantiated directly, but rather requires several parameters to be specified
 * at the time of instantiation. Various factory objects can be created which perform the appropriate mix-ins and specifications at run-time.
 *
 * The following is a list of the information that needs to be provided to a BlingConsole at instantiation:
 * 1. A [[systems.adaptix.bling.tags.TagDagSerializer]] to be mixed in, which specifies the format in which the console's member [[systems.adaptix.bling.tags.TagDag]] is to be serialized.
 * 2. The RawData abstract type to be specified, which specifies the format in which raw data will be presented to bling.
 * 3. A convertToTaggedData abstract method to be implemented, which converts RawData into TaggedData.
 * 4. A BlingData abstract type to be specified, which specifies the format in which bling is expected to present data to its users.
 * 5. A convertToBlingData abstract method to be implemented, which converts key-value pairs of data extracted from the database into BlingData.
 * 6. An implicit [[scalikejdbc.DBSession]] to be defined in the scope of instantiation.
 * 7. A dataHandler abstract value to be specified, which is an object of type derived from [[systems.adaptix.bling.data.DataHandler]].
 * 8. A tagDag abstract variable to be specified, which is an object of type derived from [[systems.adaptix.bling.tags.TagDag]].
 * 9. A tagDagFileName abstract value to be specified, which is a String containing the location of a file on the filesystem which is meant to store an up-to-date copy of the serialization of the tagDag member.
 * 10. A blingId abstract value to be specified, which is a String containing the name of the internal identification column for data in the bling data store.
 *
 * Created by nkashyap on 6/10/15.
 */
trait BlingConsole { this: TagDagSerializer =>
  type RawData
  type BlingData

  def convertToTaggedData(data: RawData): TaggedData
  def convertToBlingData(data: Map[String, Any]): BlingData

  type Tag = TagDag#Tag

  val dataHandler: DataHandler // Requires an implicit scalikejdbc.DBSession to be defined in an outer scope.
  var tagDag: TagDag

  val tagDagFileName: String
  val blingId: String

  /**
   * Loads raw data into the bling data store.
   * @param data The raw data to be loaded.
   */
  def loadData(data: RawData) = dataHandler.insert(convertToTaggedData(data))

  /**
   * Extracts data from the bling data store subject to constraints imposed by the caller and tagged with a given tag from the console's tagDag.
   *
   * @param columns The columns from the selection that the caller wants retrieved. If the caller does not care, they can pass [[systems.adaptix.bling.data.AllColumns]].
   * @param criterion A [[systems.adaptix.bling.data.SelectionCriterion]] to be imposed upon the selection. This is in addition to the tag information which is specified as a different parameter.
   * @param tag A valid tag from the tagDag member of the console. This choice specifies that the caller wishes all the data associated with this tag to be retrieved from the data store. Data is associated with the tag if it is either tagged with that tag or with one of its descendants in the tagDag.
   * @param mode By default, this is set to [[systems.adaptix.bling.DisjunctiveMode]], which specifies that all data should be selected which is tagged with at least one descendant of the specified tag parameter. It can also be set to [[systems.adaptix.bling.ConjunctiveMode]], which specifies that only data should be selected which is tagged with ALL of the descendants of the tag parameter.
   * @return The selection satisfying the given constraints and associated with the sepcified tag in the form of a Seq[BlingData].
   */
  def extractData(columns: DesiredColumns, criterion: SelectionCriterion = NoCriterion, tag: Tag = tagDag.universalTag, mode: ExtractionMode = DisjunctiveMode): Seq[BlingData] = {
    val descendantDataTags = tagDag.descendants(tag).toSet intersect dataTags
    val tagConstraint = (mode match {
      case DisjunctiveMode => Or
      case ConjunctiveMode => And
    })(
      descendantDataTags.map(tag => In(blingId, dataHandler.tagIndexers(tag).tableName, AllColumns))
        .toSeq: _*
      )
    criterion match {
      case NoCriterion => dataHandler.select(columns, tagConstraint).map( row => convertToBlingData(row) )
      case _ => dataHandler.select(columns, And(tagConstraint, criterion)).map( row => convertToBlingData(row) )
    }
  }

  /**
   * Saves the serialization of the tagDag member in the specified file.
   * @param fileName The name of the file in which the serialization should be saved. Defaults to the tagDagFileName member of the console.
   */
  def saveTagDag(fileName: String = tagDagFileName) = {
    val writer = new PrintWriter(new File(fileName))
    try writer.write(serialize(tagDag)) finally writer.close()
  }

  /**
   * Overrides the current tagDag member variable of the console with a [[systems.adaptix.bling.tags.TagDag]] deserialized from the given source file.
   * @param fileName The name of a file containing a serialized [[systems.adaptix.bling.tags.TagDag]] which the caller desires loaded as the tagDag member of the console.
   */
  def loadTagDag(fileName: String = tagDagFileName) = {
    tagDag = deserialize(Source.fromFile(fileName).mkString)
  }

  /**
   * @return All the tags that the bling data store is aware of, as a Set[String]
   */
  def dataTags: Set[Tag] = dataHandler.select(AllColumns, NoCriterion, dataHandler.tagsTemplate).map(row => row.head._2.asInstanceOf[Tag]).toSet[Tag]

  /**
   * Loads tags into the tagDag member which have been loaded into the data store but not yet registered in the tagDag.
   */
  def refreshTagDag() = dataTags.foreach( tag => {if (!(tagDag hasTag tag)) tagDag.insertTag(tag)} )
}
