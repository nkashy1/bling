package systems.adaptix.bling.data

import scalikejdbc.interpolation.SQLSyntax

/**
 * Created by nkashyap on 6/7/15.
 */

sealed trait SelectionCriterion {
  abstract def generateConstraints: (String, Seq[Any])
}




final case class AtomicCriterion(column: String, value: Any) extends SelectionCriterion {
  def generateConstraints = (s"{$column} = ?", Seq(value))
}


object And extends Junction{
  val asString = " AND "
  def apply(componentCriteria: SelectionCriterion*) = new CompoundCriterion(this, componentCriteria:_*)
}
object Or extends Junction {
  val asString = " OR "
  def apply(componentCriteria: SelectionCriterion*) = new CompoundCriterion(this, componentCriteria:_*)
}

sealed trait Junction {
  abstract def asString: String
}

final class CompoundCriterion(junction: Junction, componentCriteria: SelectionCriterion*) {
  def generateConstraints = {
    val (componentStrings, componentValues) = componentCriteria.map(_.generateConstraints).unzip
    ( componentStrings.mkString(junction.asString), ( componentValues :\ Seq[Any]() )(_ ++ _) )
  }
}
