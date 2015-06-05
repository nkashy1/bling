package systems.adaptix.bling.data

import org.specs2.mutable.Specification

import scalikejdbc.SQLSyntax

/**
 * Created by nkashyap on 6/4/15.
 */
class FieldInfoSpecification extends Specification {
  "The FieldInfo trait enables the generation of chunks of SQL, as strings, pertaining to the field being represented. These SQL chunks, fieldName and sqlTypeDeclaration, can be used to dynamically generate SQL queries. FieldInfo is in fact a union of the following types:" >> {
    "PlainFieldInfo, which represents a vanilla SQL column." in {
      val plainField = PlainFieldInfo("name", "type")
      plainField.fieldName mustEqual "name"
      plainField.sqlTypeDeclaration mustEqual "type"
    }

    "NotNullFieldInfo, which represents an SQL column which is guaranteed to contain a value." in {
      val notNullField = NotNullFieldInfo("name", "type")
      notNullField.fieldName mustEqual "name"
      notNullField.sqlTypeDeclaration mustEqual "type NOT NULL"
    }

    "PrimaryFieldInfo, which represents an SQL column holding a primary key." in {
      val primaryField = PrimaryFieldInfo("name", "type")
      primaryField.fieldName mustEqual "name"
      primaryField.sqlTypeDeclaration mustEqual "type NOT NULL PRIMARY KEY"
    }

    "AutoIdFieldInfo, which represents an automatically incrementing primary key field." in {
      val autoIdField = AutoIdFieldInfo("name")
      autoIdField.fieldName mustEqual "name"
      autoIdField.sqlTypeDeclaration mustEqual "SERIAL NOT NULL PRIMARY KEY"
    }
  }
}
