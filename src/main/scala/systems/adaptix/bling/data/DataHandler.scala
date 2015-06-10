package systems.adaptix.bling.data

import scalikejdbc._

/**
 * Created by nkashyap on 6/5/15.
 */
class DataHandler(val dataTemplate: TableTemplate, val tagsTemplate: TagTableTemplate)(implicit session: scalikejdbc.DBSession) {
  val columnNameSet = dataTemplate.columns.map(_.fieldName).toSet[String]
  val requiredFieldNameSet = dataTemplate.columns.filter({
    case _: SerialField => false
    case _: NotNullField => true
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

  var tagIndexers = Map[String, IdTableTemplate]()
  def loadTagIndexers = {
    /*
     TODO: Right now, this is creating new IdTableTemplates for each of the tags, regardless of whether they were previous registered or not. In the future, we may want to introduce a check for when a new IdTableTemplate is required.
      */
    val existingTags = sql"SELECT * FROM ${tagsTemplate.sqlTableName}".map(_.toMap).list.apply().map(_.head._2.toString)
    tagIndexers = Map[String, IdTableTemplate](existingTags zip existingTags.map( tag => new IdTableTemplate(tag, "id") ) :_*)
  }
  loadTagIndexers
  tagIndexers.foreach( pair => createTableIfItDoesNotExist(pair._2) )

  def insert(input: TaggedData) = {
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
    val sqlTag = SQLSyntax.createUnsafely(tag)
    val tagIndexer = new IdTableTemplate(tag, "id")
    sql"SELECT ${tagsTemplate.sqlColumnNames} FROM ${tagsTemplate.sqlTableName} WHERE ${tagsTemplate.sqlColumnNames}=${tag}".map(_.toMap).first.apply().getOrElse(
      sql"INSERT INTO ${tagsTemplate.sqlTableName} (${tagsTemplate.sqlColumnNames}) VALUES (${tag})".update.apply()
    )
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

  def select(targetColumns: DesiredColumns, criterion: SelectionCriterion = NoCriterion, tableTemplate: TableTemplate = dataTemplate): Seq[Map[String, Any]] = {
    val columnsSql = SQLSyntax.createUnsafely(s"${targetColumns.asString}")

    criterion match {
      case NoCriterion => sql"SELECT ${columnsSql} FROM ${tableTemplate.sqlTableName}".map(_.toMap).list.apply()
      case _ => {
        val (criterionSqlSyntax, criterionValuesToBind) = criterion.asSqlSyntaxWithValuesToBind
        sql"SELECT ${columnsSql} FROM ${tableTemplate.sqlTableName} WHERE ${criterionSqlSyntax}".bind(criterionValuesToBind:_*).map(_.toMap).list.apply()
      }
    }
  }
}
