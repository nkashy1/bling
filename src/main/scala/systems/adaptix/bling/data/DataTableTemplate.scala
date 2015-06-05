package systems.adaptix.bling.data

import scalikejdbc._
/**
 * Created by nkashyap on 6/4/15.
 */

class DataTableTemplate(val tableName: String, val columns: Seq[DataFieldInfo]) {
  def sqlCreate = {
    val columnDefinitions = columns.map(column => column.fieldName + " " + column.sqlTypeDeclaration).mkString(", ")
    val creationString = s"CREATE TABLE ${tableName} (${columnDefinitions})"
    SQLSyntax.createUnsafely(creationString)
  }

  def sqlDrop = {
    SQLSyntax.createUnsafely(s"DROP TABLE ${tableName}")
  }
}
