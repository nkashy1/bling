package systems.adaptix.bling.data

import scalikejdbc._

/**
 * Created by nkashyap on 6/5/15.
 */
class Absorber(val dataTemplate: TableTemplate, val tagsTemplate: TagTableTemplate)(implicit session: scalikejdbc.DBSession) {
  val columnNameSet = dataTemplate.columns.map(_.fieldName).toSet[String]
  val requiredFieldNameSet = dataTemplate.columns.filter({
    case _: SerialField => false
    case _: NotNull => true
    case _ => false
  }).map(_.fieldName).toSet[String]

  private def createTableIfItDoesNotExist(template: TableTemplate) = {
    try {
      sql"${template.sqlCreate}".execute.apply()
    } catch {
      case _: Exception =>
    }
  }

  createTableIfItDoesNotExist(dataTemplate)
  createTableIfItDoesNotExist(tagsTemplate)

  private def loadTagIndexers: Map[String, IdTableTemplate] = {
    val existingTags = sql"SELECT * FROM ${tagsTemplate.sqlTableName}".map(_.toMap).list.apply().map(_.head._2.toString)
    Map[String, IdTableTemplate](existingTags zip existingTags.map( tag => new IdTableTemplate(tag, "id") ) :_*)
  }
  var tagIndexers: Map[String, IdTableTemplate] = loadTagIndexers
  tagIndexers.foreach( pair => createTableIfItDoesNotExist(pair._2) )

  def absorb(input: TaggedData) = {
    val rowId = insertDataAndGetId(input.data)
    val relevantTagIndexers = input.tags.map(getIdTableTemplate)
    relevantTagIndexers.foreach( indexer => indexId(rowId, indexer) )
  }

  private def insertDataAndGetId(data: Map[String, Any]): Long = {
    val columnsSql = SQLSyntax.createUnsafely( data.keys.map( key => s"${key}" ).mkString(", ") )
    val valuesPlaceHolderSql = SQLSyntax.createUnsafely( Seq.fill(data.keys.size)("?").mkString(", ") )
    sql"INSERT INTO ${dataTemplate.sqlTableName} (${columnsSql}) VALUES (${valuesPlaceHolderSql})".
      bind(data.values.toList:_*).updateAndReturnGeneratedKey().apply()
  }

  private def getIdTableTemplate(tag: String) = {
    tagIndexers.getOrElse(tag, registerNewTag(tag))
  }

  private def registerNewTag(tag: String) = {
    val tagIndexer = new IdTableTemplate(tag, "id")
    createTableIfItDoesNotExist(tagIndexer)
    tagIndexers = tagIndexers + (tag -> tagIndexer)
    tagIndexer
  }

  private def indexId(id: Long, tagIndexer: IdTableTemplate) = {
    val columnSql = SQLSyntax.createUnsafely(tagIndexer.columnName)
    sql"INSERT INTO ${tagIndexer.sqlTableName} (${columnSql}) VALUES (?)".bind(id).update().apply()
  }

  def validateFields(input: TaggedData): Boolean = {
    (input.data.keySet subsetOf columnNameSet) && (requiredFieldNameSet subsetOf input.data.keySet)
  }
}
