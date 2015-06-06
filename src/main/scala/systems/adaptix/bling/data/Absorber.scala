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
    val existingTags = sql"SELECT * FROM ${tagsTemplate.sqlTableName}".list.apply()
    Map[String, IdTableTemplate](existingTags zip existingTags.map( tag => new IdTableTemplate(tag, "id") ) :_*)
  }
  var tagIndexers = loadTagIndexers
  tagIndexers.foreach( pair => createTableIfItDoesNotExist(pair._2) )

  def validateInput(input: TaggedData): Boolean = {
    (input.tags subsetOf columnNameSet) && (requiredFieldNameSet subsetOf input.tags)
  }

  def insertDataAndGetId(input: TaggedData): Long = {
    val columnsString = input.data.keys.map( key => s"${key}" ).mkString(", ")
    val valuesPlaceHoldersString = input.data.keys.map ( _ => "?" ).mkString(", ")
    val insertionString = s"INSERT INTO ${dataTemplate.tableName} (${columnsString}) VALUES (${valuesPlaceHoldersString})"
    sql"${insertionString}".bind(input.data.values.toSeq:_*).updateAndReturnGeneratedKey().apply()
  }

  def getIdTableTemplate(tag: String) = {
    tagIndexers.getOrElse(tag, registerNewTag(tag))
  }

  def registerNewTag(tag: String) = {
    val tagIndexer = new IdTableTemplate(tag, "id")
    tagIndexers = tagIndexers + (tag -> tagIndexer)
    tagIndexer
  }
}
