package systems.adaptix.bling.data

import scalikejdbc._

/**
 * Created by nkashyap on 6/7/15.
 */

// TODO: EXISTS queries

/**
 *
 */
sealed trait SelectionCriterion {
  // TODO: There could be an issue here about how values are bound. Depends on the SQL parser. If parsing is done left-to-right, then what this method is doing now with compound constraints is alright.
  def generateConstraints: (String, Seq[Any])

  def asSqlSyntaxWithValuesToBind: (SQLSyntax, Seq[Any]) = {
    val (constraints, values) = generateConstraints
    (SQLSyntax.createUnsafely(constraints), values)
  }
}


object NoCriterion extends SelectionCriterion {
  def generateConstraints = ("", Seq())
}


object Eq extends OrderConstraint
object Ne extends OrderConstraint
object Lt extends OrderConstraint
object Le extends OrderConstraint
object Gt extends OrderConstraint
object Ge extends OrderConstraint

sealed trait OrderConstraint {
  def asString = this match {
    case Eq => "="
    case Ne => "<>"
    case Lt => "<"
    case Le => "<="
    case Gt => ">"
    case Ge => ">="
  }

  def apply(column: String, value: Any): SelectionCriterion = OrderCriterion(this, column, value)
}

final case class OrderCriterion(constraintType: OrderConstraint, column: String, value: Any) extends SelectionCriterion {
  def generateConstraints = (s"${column} ${constraintType.asString} ?", Seq(value))
}


object Null {
  def apply(column: String) = NullCriterion(true, column)
}
object NotNull {
  def apply(column: String) = NullCriterion(false, column)
}

final case class NullCriterion(isNull: Boolean, column: String) extends SelectionCriterion {
  def generateConstraints = (s"${column}" + {if (isNull) " IS NULL" else " IS NOT NULL"}, Seq[Any]())
}


object In {
  def apply(column: String, tableName: String, tableColumns: DesiredColumns = AllColumns, tableCriterion: SelectionCriterion = NoCriterion) = InCriterion(column, tableName, tableColumns, tableCriterion)
}

final case class InCriterion(column: String, tableName: String, tableColumns: DesiredColumns, tableCriterion: SelectionCriterion) extends SelectionCriterion {
  def generateConstraints = tableCriterion match {
    case NoCriterion => (s"${column} IN (SELECT ${tableColumns.asString} FROM ${tableName})", Seq[Any]())
    case _ => {
      val (criterionString, criterionValuesToBind) = tableCriterion.generateConstraints
      (s"${column} IN (SELECT ${tableColumns.asString} FROM ${tableName} WHERE ${criterionString})", criterionValuesToBind)
    }
  }
}

sealed trait DesiredColumns {
  def asString = this match {
    case AllColumns => "*"
    case desired: SomeColumns => desired.columns.mkString(", ")
  }
}

object AllColumns extends DesiredColumns
final case class SomeColumns(columns: Seq[String]) extends DesiredColumns


sealed trait Junction {
  def asString = this match {
    case And => "AND"
    case Or => "OR"
  }

  def apply(componentCriteria: SelectionCriterion*): SelectionCriterion = new JunctiveCriterion(this, componentCriteria:_*)
}

object And extends Junction
object Or extends Junction

final class JunctiveCriterion(junction: Junction, componentCriteria: SelectionCriterion*) extends SelectionCriterion {
  def generateConstraints = {
    val (componentStrings, componentValues) = componentCriteria.map(_.generateConstraints).unzip
    ( "(" + componentStrings.mkString(s") ${junction.asString} (") + ")" , ( componentValues :\ Seq[Any]() )(_ ++ _) )
  }
}


object Not {
  def apply(criterionToNegate: SelectionCriterion): SelectionCriterion = NegativeCriterion(criterionToNegate)
}

final case class NegativeCriterion(componentCriterion: SelectionCriterion) extends SelectionCriterion {
  def generateConstraints = {
    val (componentString, componentValues) = componentCriterion.generateConstraints
    ( s"NOT (${componentString})", componentValues)
  }
}
