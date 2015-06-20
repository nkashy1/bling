package systems.adaptix.bling.data

/**
 * Created by nkashyap on 6/4/15.
 */

/**
 * FieldProperty and its subtraits are used internally to designate characteristics of fields beyond their name and type.
 */
sealed trait FieldProperty

/**
 * A PlainField is one which has no distinguishing characteristics.
 */
sealed trait PlainField extends FieldProperty

/**
 * A PrimaryKey is intended to be used as the primary key in a table.
 */
sealed trait PrimaryKey extends FieldProperty

/**
 * A SerialField is one which is automatically specified in the database.
 */
sealed trait SerialField extends FieldProperty {
  val fieldType = "SERIAL"
}

/**
 * A NotNullField is one which may not contain any NULL values.
 */
sealed trait NotNullField extends FieldProperty

/**
 * A FieldInfo object represents a column of a table in a database. It is a union of the following types:
 * 1. PlainFieldInfo
 * 2. NotNullFieldInfo
 * 3. PrimaryFieldInfo
 * 4. AutoIdFieldInfo
 *
 * FieldInfo objects expose the name of the fields they represent as well as the type of field via their fieldName and fieldType members.
 */
sealed trait FieldInfo {this: FieldProperty =>
  def fieldName: String
  def fieldType: String

  /**
   * @return A string containing the field definition as it would appear in an SQL table schema.
   */
  def sqlTypeDeclaration = {
    Map(fieldType -> true, "SERIAL" -> isSerialField, "NOT NULL" -> (isNotNull), "PRIMARY KEY" -> isPrimaryKey).
      filter(_._2 == true).
      keys.
      mkString(" ")
  }

  /**
   * @return true if the field is a PrimaryKey and false otherwise.
   */
  private def isPrimaryKey: Boolean = this match {
    case _: PrimaryKey => true
    case _ => false
  }

  /**
   * @return true if the field is a SerialField and false otherwise.
   */
  private def isSerialField: Boolean = this match {
    case _: SerialField => true
    case _ => false
  }

  /**
   * @return true if the field is a NotNullField and false otherwise.
   */
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
