package systems.adaptix.bling.data

import org.specs2.mutable.Specification

import scalikejdbc.SQLSyntax

/**
 * Created by nkashyap on 6/4/15.
 */
class DataFieldInfoSpecification extends Specification {
  "The DataFieldInfo trait enables the generation of chunks of SQL pertaining to the field being represented. These SQL chunks, sqlColumnName and sqlTypeDeclaration, can be used to dynamically generate SQL queries. DataFieldInfo is in fact a union of the following types:" >> {
    "PlainFieldInfo, which represents a vanilla SQL column." in {
      val plainField = PlainFieldInfo("name", "type")
      plainField.sqlColumnName mustEqual SQLSyntax.createUnsafely("name")
      plainField.sqlTypeDeclaration mustEqual SQLSyntax.createUnsafely("type")
    }

    "NotNullFieldInfo, which represents an SQL column which is guaranteed to contain a value." in {
      val notNullField = NotNullFieldInfo("name", "type")
      notNullField.sqlColumnName mustEqual SQLSyntax.createUnsafely("name")
      notNullField.sqlTypeDeclaration mustEqual SQLSyntax.createUnsafely("type NOT NULL")
    }

    "PrimaryFieldInfo, which represents an SQL column holding a primary key." in {
      val primaryField = PrimaryFieldInfo("name", "type")
      primaryField.sqlColumnName mustEqual SQLSyntax.createUnsafely("name")
      primaryField.sqlTypeDeclaration mustEqual SQLSyntax.createUnsafely("type NOT NULL PRIMARY KEY")
    }

    "AutoIdFieldInfo, which represents an automatically incrementing primary key field." in {
      val autoIdField = AutoIdFieldInfo("name")
      autoIdField.sqlColumnName mustEqual SQLSyntax.createUnsafely("name")
      autoIdField.sqlTypeDeclaration mustEqual SQLSyntax.createUnsafely("SERIAL NOT NULL PRIMARY KEY")
    }
  }
}
