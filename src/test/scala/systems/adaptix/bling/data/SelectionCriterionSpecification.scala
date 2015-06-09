package systems.adaptix.bling.data

import org.specs2.mutable.Specification

import scalikejdbc._

/**
 * Created by nkashyap on 6/8/15.
 */
class SelectionCriterionSpecification extends Specification {
  """SelectionCriterion is an algebraic data type. It is a union of the following subtypes:
    | 1. NoCriterion
    | 2. OrderCriterion,
    | 3. NullCriterion,
    | 4. InCriterion
    | 5. JunctiveCriterion,
    | 6. NegativeCriterion.
    |
    | A SelectionCriterion provides two methods:
    | 1. generateConstraints returns an ordered pair of type (String, Seq[Any]). The String represents the portion of an SQL SELECT statement occurring after a WHERE clause with \"?\"s where values are to be specified. The Seq[Any] contains the actual values being specified.
    | 2. asSqlSyntaxWithValuesToBind returns an ordered pair of type (scalikejdbc.SQLSyntax, Seq[Any]) by simply converting the first coordinate of the generateConstraints return value into SQLSyntax.
    |
    | SelectionCriteria are meant to be constructed by calling the associated objects -- Eq, Ne, Lt, Le, Gt, Ge, In, Null, NotNull, And, Or, and Not.
  """.stripMargin >> {
    "Eq, Ne, Lt, Le, Gt, Ge, Null, and NotNull generate atomic SelectionCriteria in the sense that they represent direct constraints on column values." >> {
      val column = "answer"
      val value = 42
      val valueSequence = Seq[Any](value)
      val emptySequence = Seq[Any]()

      "Eq generates a SelectionCriterion representing the constraint that the value in the given column be EQUAL to the given value." >> {
        val equalConstraint = Eq(column, value)
        equalConstraint.generateConstraints mustEqual (s"${column} = ?", valueSequence)
      }

      "Ne generates a SelectionCriterion representing the constraint that the value in the given column be NOT EQUAL to the given value." >> {
        val notEqualConstraint = Ne(column, value)
        notEqualConstraint.generateConstraints mustEqual (s"${column} <> ?", valueSequence)
      }

      "Lt generates a SelectionCriterion representing the constraint that the value in the given column be LESS THAN the given value." >> {
        val lessThanConstraint = Lt(column, value)
        lessThanConstraint.generateConstraints mustEqual (s"${column} < ?", valueSequence)
      }

      "Le generates a SelectionCriterion representing the constraint that the value in the given column be LESS THAN OR EQUAL TO the given value." >> {
        val lessThanOrEqualToConstraint = Le(column, value)
        lessThanOrEqualToConstraint.generateConstraints mustEqual (s"${column} <= ?", valueSequence)
      }

      "Gt generates a SelectionCriterion representing the constraint that the value in the given column be GREATER THAN the given value." >> {
        val greaterThanConstraint = Gt(column, value)
        greaterThanConstraint.generateConstraints mustEqual (s"${column} > ?", valueSequence)
      }

      "Ge generates a SelectionCriterion representing the constraint that the value in the given column be GREATER THAN OR EQUAL TO the given value." >> {
        val greaterThanOrEqualToConstraint = Ge(column, value)
        greaterThanOrEqualToConstraint.generateConstraints mustEqual (s"${column} >= ?", valueSequence)
      }

      "Null generates a SelectionCriterion representing the constraint that the value in the given column be NULL." >> {
        val nullConstraint = Null(column)
        nullConstraint.generateConstraints mustEqual (s"${column} IS NULL", emptySequence)
      }

      "NotNull generates a SelectionCriterion representing the constraint that the value in the given column NOT be NULL." >> {
        val notNullConstraint = NotNull(column)
        notNullConstraint.generateConstraints mustEqual (s"${column} IS NOT NULL", emptySequence)
      }
    }

    "In generates a SelectionCriterion representing the constraint that the value in the given column be present in the selection of given tableColumns from the table with given tableName which satisfy the given tableCriterion." >> {
      val column = "ANSWER"
      val tableName = "OTHER_TABLE"
      val tableColumns = SomeColumns(Seq("SPECIFIC_COLUMNS"))
      val otherColumn = "ANOTHER_SPECIFIC_COLUMN"
      val otherColumnValue = 0
      val tableConstraint = Eq(otherColumn, otherColumnValue)

      "It can be used to generate a constraint specifying that the value in a given column simply be in the specified column of the specified table." >> {
        val constraint = In(column, tableName, tableColumns).generateConstraints
        constraint mustEqual (s"${column} IN (SELECT ${tableColumns.asString} FROM ${tableName})", Seq[Any]())
      }

      "Or even by imposing a further constraint on the inner selection." >> {
        val constraint = In(column, tableName, tableColumns, tableConstraint).generateConstraints
        val subConstraint = tableConstraint.generateConstraints
        constraint mustEqual (s"${column} IN (SELECT ${tableColumns.asString} FROM ${tableName} WHERE ${subConstraint._1})", subConstraint._2)
      }
    }

    "And, Or, and Not generate compound SelectionCriteria in the sense that they represent logical operations on other SelectionCriteria." >> {
      val col1 = "answer"
      val val1 = 42
      val col2 = "question"
      val val2 = "?"
      val col3 = "name"
      val val3 = "bob"
      val threeAtomicConstraints = Seq(Lt(col1, val1), Ne(col2, val2), Eq(col3, val3))
      val threeAtomicValueSequence = Seq[Any](val1, val2, val3)

      "And accepts a variable number of SelectionCriteria as arguments and generates a SelectionCriterion representing the constraint obtained by imposing ALL of the constraints represented by the inputs." >> {
        val andConstraint = And(threeAtomicConstraints:_*)
        andConstraint.generateConstraints mustEqual (s"(${col1} < ?) AND (${col2} <> ?) AND (${col3} = ?)", threeAtomicValueSequence)
      }

      "Or accepts a variable number of SelectionCriteria as arguments and generates a SelectionCriterion representing the constrained obtained by requiring that AT LEAST ONE of the constraints represented by the inputs hold." >> {
        val orConstraint = Or(threeAtomicConstraints:_*)
        orConstraint.generateConstraints mustEqual (s"(${col1} < ?) OR (${col2} <> ?) OR (${col3} = ?)", threeAtomicValueSequence)
      }

      "Not accepts a single SelectionCriterion and generates a SelectionCriterion representing the NEGATION of the constraint represented by its input." >> {
        val notConstraint = Not(Eq(col1, val1))
        notConstraint.generateConstraints mustEqual (s"NOT (${col1} = ?)", Seq[Any](val1))
      }

      "Each of these objects can also accept compound SelectionCriteria as their inputs." >> {
        val constraint = Or( Not(Eq(col1, val1)), And(Eq(col1, val1), Eq(col2, val2), Eq(col3, val3)) )
        constraint.generateConstraints mustEqual (s"(NOT (${col1} = ?)) OR ((${col1} = ?) AND (${col2} = ?) AND (${col3} = ?))", Seq[Any](val1, val1, val2, val3))
      }
    }

    "There is also a NoCriterion singleton object which reflects that no constraints are being imposed on a selection." >> {
      NoCriterion.generateConstraints mustEqual ("", Seq())
      NoCriterion.asSqlSyntaxWithValuesToBind
      ok
    }
  }
}
