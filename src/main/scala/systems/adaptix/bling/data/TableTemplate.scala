package systems.adaptix.bling.data

import scalikejdbc._
/**
 * Created by nkashyap on 6/4/15.
 */

class TableTemplate(val tableName: String, val columns: Seq[FieldInfo]) {
  def sqlTableName = SQLSyntax.createUnsafely(tableName)

  def schema = columns.map( column =>
    s"${column.fieldName} ${column.sqlTypeDeclaration}"
    ).mkString(", ")

  def columnNames = s"(${ columns.map(_.fieldName).mkString(", ") })"
  def sqlColumnNames = SQLSyntax.createUnsafely(columnNames)

  def nonAutoIdColumnNames = s"(${ columns.filter( {
    case _: AutoIdFieldInfo => false
    case _ => true
    } ).map(_.fieldName).mkString(", ") })"
  def sqlNonAutoIdColumnNames = SQLSyntax.createUnsafely(nonAutoIdColumnNames)

  def create(implicit session: scalikejdbc.DBSession) = sql"${ sqlCreate }".execute.apply()
  def sqlCreate = {
    val columnDefinitions = columns.map(column => column.fieldName + " " + column.sqlTypeDeclaration).mkString(", ")
    val creationString = s"CREATE TABLE ${tableName} (${columnDefinitions})"
    SQLSyntax.createUnsafely(creationString)
  }

  def drop(implicit session: scalikejdbc.DBSession) = sql"${ sqlDrop }".execute.apply()
  def sqlDrop = {
    SQLSyntax.createUnsafely(s"DROP TABLE ${tableName}")
  }
}
