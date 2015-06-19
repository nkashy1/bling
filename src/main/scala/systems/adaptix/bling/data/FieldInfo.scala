package systems.adaptix.bling.data

/**
 * Created by nkashyap on 6/4/15.
 */

sealed trait FieldProperty
sealed trait PlainField extends FieldProperty
sealed trait PrimaryKey extends FieldProperty
sealed trait SerialField extends FieldProperty {
  val fieldType = "SERIAL"
}
sealed trait NotNullField extends FieldProperty

sealed trait FieldInfo {this: FieldProperty =>
  def fieldName: String

  def fieldType: String
  def sqlTypeDeclaration = {
    Map(fieldType -> true, "SERIAL" -> isSerialField, "NOT NULL" -> (isNotNull), "PRIMARY KEY" -> isPrimaryKey).
      filter(_._2 == true).
      keys.
      mkString(" ")
  }

  private def isPrimaryKey: Boolean = this match {
    case _: PrimaryKey => true
    case _ => false
  }

  private def isSerialField: Boolean = this match {
    case _: SerialField => true
    case _ => false
  }

  private def isNotNull: Boolean = this match {
    case _: NotNullField => true
    case _ => false
  }
}

/**
 * A PlainFieldInfo object represents a regular column in a table.
 * @param fieldName The name of the column.
 * @param fieldType The type of data that the column will hold.
 */
final case class PlainFieldInfo(fieldName: String, fieldType: String) extends FieldInfo with PlainField

/**
 * A NotNullFieldInfo object represents a column in a table which is allowed to contain no NULL values.
 * @param fieldName The name of the column.
 * @param fieldType The type of data that the column will hold.
 */
final case class NotNullFieldInfo(fieldName: String, fieldType: String) extends FieldInfo with NotNullField

/**
 * A PrimaryFieldInfo object represents a column meant to hold a primary key for a table.
 * @param fieldName The name of the column.
 * @param fieldType The type of data that the column will hold.
 */
final case class PrimaryFieldInfo(fieldName: String, fieldType: String) extends FieldInfo with PrimaryKey with NotNullField

/**
 * An AutoIdFieldInfo object represents a column in a table which is an automatically generated primary key.
 * @param fieldName
 */
final case class AutoIdFieldInfo(fieldName: String) extends FieldInfo with PrimaryKey with SerialField with NotNullField
