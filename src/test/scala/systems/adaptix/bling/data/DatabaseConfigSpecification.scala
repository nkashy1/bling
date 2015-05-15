package systems.adaptix.bling.data

import org.specs2.mutable._

import com.typesafe.config._
import slick.jdbc.JdbcBackend._
import slick.driver._

/**
 * Created by nkashyap on 5/15/15.
 */
class DatabaseConfigSpecification extends Specification {
  "Connect to database specified by \"blingtestdb\" in configuration." >> {
    val db = Database.forConfig("blingtestdb")
    true
  }
}
