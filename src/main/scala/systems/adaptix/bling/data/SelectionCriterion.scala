package systems.adaptix.bling.data

import scalikejdbc._

/**
 * Created by nkashyap on 6/7/15.
 */

sealed trait SelectionCriterion {
  def generateConstraints: (String, Seq[Any])

  def asSqlSyntaxWithValuesToBind: (SQLSyntax, Seq[Any]) = {
    val (constraints, values) = generateConstraints
    (SQLSyntax.createUnsafely(constraints), values)
  }
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
  def generateConstraints = (s"{$column} ${constraintType.asString} ?", Seq(value))
}


object Null {
  def apply(column: String) = NullCriterion(true, column)
}
object NotNull {
  def apply(column: String) = NullCriterion(false, column)
}

final case class NullCriterion(isNull: Boolean, column: String) extends SelectionCriterion {
  def generateConstraints = (s"${column} IS ${if (isNull) "" else " NOT "} NULL", Seq[Any]())
}


sealed trait Junction {
  def asString = this match {
    case And => " AND "
    case Or => " OR "
  }

  def apply(componentCriteria: SelectionCriterion*): SelectionCriterion = new JunctiveCriterion(this, componentCriteria:_*)
}

object And extends Junction
object Or extends Junction

final class JunctiveCriterion(junction: Junction, componentCriteria: SelectionCriterion*) extends SelectionCriterion {
  def generateConstraints = {
    val (componentStrings, componentValues) = componentCriteria.map(_.generateConstraints).unzip
    ( componentStrings.mkString(junction.asString), ( componentValues :\ Seq[Any]() )(_ ++ _) )
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
