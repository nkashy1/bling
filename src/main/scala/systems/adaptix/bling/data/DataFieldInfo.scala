package systems.adaptix.bling.data

import scalikejdbc._

/**
 * Created by nkashyap on 6/4/15.
 */

sealed trait FieldProperty
sealed trait PlainField extends FieldProperty
sealed trait PrimaryKey extends FieldProperty
sealed trait SerialField extends FieldProperty {
  val fieldType = "SERIAL"
}
sealed trait NotNull extends FieldProperty

sealed trait DataFieldInfo {this: FieldProperty =>
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
    case _: NotNull => true
    case _ => false
  }
}

final case class PlainFieldInfo(fieldName: String, fieldType: String) extends DataFieldInfo with PlainField
final case class NotNullFieldInfo(fieldName: String, fieldType: String) extends DataFieldInfo with NotNull
final case class PrimaryFieldInfo(fieldName: String, fieldType: String) extends DataFieldInfo with PrimaryKey with NotNull
final case class AutoIdFieldInfo(fieldName: String) extends DataFieldInfo with PrimaryKey with SerialField with NotNull
