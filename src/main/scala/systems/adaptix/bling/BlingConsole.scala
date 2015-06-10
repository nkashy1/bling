package systems.adaptix.bling

import data._
import tags._

import java.io._
import scala.io.Source

/**
 * Created by nkashyap on 6/10/15.
 */
trait BlingConsole { this: TagDagSerializer =>
  type RawData
  type BlingData

  type Tag = TagDag#Tag

  val session: scalikejdbc.DBSession // Can be defined implicitly at instantiation as: val session = implicitly[scalikejdbc.DBSession]
  val dataHandler: DataHandler
  var tagDag: TagDag

  val tagDagFileName: String
  val blingId: String

  def convertToTaggedData(data: RawData): TaggedData
  def convertToBlingData(data: Map[String, Any]): BlingData

  def loadData(data: RawData) = dataHandler.insert(convertToTaggedData(data))
  def extractData(columns: DesiredColumns, criterion: SelectionCriterion = NoCriterion, tag: Tag = tagDag.universalTag): Seq[BlingData] = {
    val descendantDataTags = tagDag.descendants(tag).toSet intersect dataTags
    val tagConstraint = And(
      descendantDataTags.map( tag => In(blingId, dataHandler.tagIndexers(tag).tableName, AllColumns) )
        .toSeq:_*
    )
    dataHandler.select(columns, And(tagConstraint, criterion)).map( row => convertToBlingData(row) )
  }

  def saveTagDag(fileName: String = tagDagFileName) = {
    val writer = new PrintWriter(new File(fileName))
    try writer.write(serialize(tagDag)) finally writer.close()
  }
  def loadTagDag(fileName: String = tagDagFileName) = {
    tagDag = deserialize(Source.fromFile(fileName).mkString)
  }

  def dataTags: Set[Tag] = dataHandler.select(AllColumns, NoCriterion, dataHandler.tagsTemplate).map(row => row.head._2.asInstanceOf[Tag]).toSet[Tag]
  def refreshTagDag = dataTags.foreach( tag => {if (!(tagDag hasTag tag)) tagDag.insertTag(tag)} )
}