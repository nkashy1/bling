package systems.adaptix.bling.data

import scalikejdbc._

/**
 * TableTemplates are bling's internal representations of tables in the data store.
 *
 * A TableTemplate takes two parameters at instantiation:
 *
 * 1. The name of the table being represented by the TableTemplate object, as a String.
 *
 * 2. A Seq[FieldInfo] specifying the columns of the tables and their properties.
 *
 * Created by nkashyap on 6/4/15.
 */
class TableTemplate(val tableName: String, val columns: Seq[FieldInfo]) {
  /**
   * @return The table name as a scalikejdbc.SQLSyntax object.
   */
  def sqlTableName = SQLSyntax.createUnsafely(tableName)

  /**
   * @return The table schema.
   */
  def schema = columns.map( column =>
    s"${column.fieldName} ${column.sqlTypeDeclaration}"
    ).mkString(", ")

  /**
   * Returns a String containing the names of the columns in the represented table, separated by commas.
   * @return Names of the columns in the table.
   */
  def columnNames = s"${ columns.map(_.fieldName).mkString(", ") }"

  /**
   * Returns the output of columnNames as a scalikejdbc.SQLSyntax object.
   * @return SQLSyntax representation of comma-separated column names in the table.
   */
  def sqlColumnNames = SQLSyntax.createUnsafely(columnNames)

  /**
   * Returns a comma-separated list of the column names in the table which aren't managed by bling.
   * @return Names of the columns in the table except for the bling ID column.
   */
  def nonAutoIdColumnNames = s"${ columns.filter( {
    case _: AutoIdFieldInfo => false
    case _ => true
    } ).map(_.fieldName).mkString(", ") }"

  /**
   * Returns the output of nonAutoIdColumnNames as a scalikejdbc.SQLSyntax object.
   * @return SQLSyntax representation of comma-separated column names which are not the bling ID column.
   */
  def sqlNonAutoIdColumnNames = SQLSyntax.createUnsafely(nonAutoIdColumnNames)

  /**
   * Creates the represented table in the data store.
   * @param session The connection to the data store.
   */
  def create(implicit session: scalikejdbc.DBSession) = sql"${ sqlCreate }".execute.apply()

  /**
   * Generates scalikejdbc.SQLSyntax object which can be executed to create the represented table in a data store.
   * @return SQLSyntax object for table creation.
   */
  def sqlCreate = {
    val columnDefinitions = columns.map(column => column.fieldName + " " + column.sqlTypeDeclaration).mkString(", ")
    val creationString = s"CREATE TABLE ${tableName} (${columnDefinitions})"
    SQLSyntax.createUnsafely(creationString)
  }

  /**
   * Drops the represented table from the data store.
   * @param session The connection to the data store.
   */
  def drop(implicit session: scalikejdbc.DBSession) = sql"${ sqlDrop }".execute.apply()

  /**
   * Generates scalikejdbc.SQLSyntax object which can be executed to drop the represented table from a data store.
   * @return SQLSyntax object for table dropping.
   */
  def sqlDrop = {
    SQLSyntax.createUnsafely(s"DROP TABLE ${tableName}")
  }
}

/**
 * Represents the table in the bling data store which stores the tags that have been loaded into bling.
 * @param tableName The name of the table in which tag information should be stored.
 * @param columnName The name of the single column of this table.
 */
class TagTableTemplate(tableName: String, val columnName: String) extends TableTemplate(tableName, Seq( PrimaryFieldInfo(columnName, "VARCHAR") ))

/**
 * Represents the tables in the bling data store corresponding to the tags. Each such table stores the bling IDs of the data points tagged with the relevant tag.
 * @param tableName The name of the table, which should really be the tag which it is tracking.
 * @param columnName The name of the single column of this table.
 */
class IdTableTemplate(tableName: String, val columnName: String) extends TableTemplate(tableName, Seq( PrimaryFieldInfo(columnName, "BIGINT") ))
