package systems.adaptix.bling.data

import scalikejdbc._

/**
 * DataHandlers implement the nuts and the bolts of actually loading data into and extracting data from a bling data store. As such,
 * a DataHandler requires three parameters to be passed to it at instantiation:
 * 1. A [[systems.adaptix.bling.data.TableTemplate]] which specifies the name and schema of the table which will store the data to be loaded into the data store.
 * 2. A [[systems.adaptix.bling.data.TagTableTemplate]] which specifies the name and schema of the table in which the tags are to be stored.
 * 3. An implicit scalikejdbc.DBSession through which the DataHandler communicates with the database.
 *
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

  /**
   * Refreshes the tag indexer information stored in the DataHandler to reflect the state of the table in the database which stores tags.
   */
  def loadTagIndexers = {
    /*
     TODO: Right now, this is creating new IdTableTemplates for each of the tags, regardless of whether they were previous registered or not. In the future, we may want to introduce a check for when a new IdTableTemplate is required.
      */
    val existingTags = sql"SELECT * FROM ${tagsTemplate.sqlTableName}".map(_.toMap).list.apply().map(_.head._2.toString)
    tagIndexers = Map[String, IdTableTemplate](existingTags zip existingTags.map( tag => new IdTableTemplate(tag, "id") ) :_*)
  }
  loadTagIndexers
  tagIndexers.foreach( pair => createTableIfItDoesNotExist(pair._2) )

  /**
   * Loads data into the DataHandler's implicit session.
   * @param input A [[systems.adaptix.bling.data.TaggedData]] object representing the data to be loaded.
   */
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

  /**
   * Checks whether given data is consistent with dataTemplate schema.
   * @param input A [[systems.adaptix.bling.data.TaggedData]] object which the caller would like to check for consistency with the data table schema.
   * @return true if the input is consistent with the data table schema and false otherwise.
   */
  def validateFields(input: TaggedData): Boolean = {
    (input.data.keySet subsetOf columnNameSet) && (requiredFieldNameSet subsetOf input.data.keySet)
  }

  /**
   * Selects data from the DataHandler's implicit session.
   * @param targetColumns A [[systems.adaptix.bling.data.DesiredColumns]] object specifying the columns the caller would like to select.
   * @param criterion A [[systems.adaptix.bling.data.SelectionCriterion]] object representing the selection constraints.
   * @param tableTemplate The [[systems.adaptix.bling.data.TableTemplate]] of the table from which data is to be selected. This defaults to the dataTemplate member of the DataHandler.
   * @return
   */
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
