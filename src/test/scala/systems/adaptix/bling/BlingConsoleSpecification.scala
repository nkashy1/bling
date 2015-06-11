package systems.adaptix.bling

import org.specs2.mutable.Specification
import org.specs2.specification._

import scalikejdbc._
import scalikejdbc.config._

import data._
import tags._

/**
 * Created by nkashyap on 6/10/15.
 */
class BlingConsoleSpecification extends Specification with AfterAll {
  sequential

  DBs.setupAll()
  implicit val outerSession = AutoSession

  val blingId = "BLING_ID"
  val id = AutoIdFieldInfo(blingId)
  val name = PlainFieldInfo("NAME", "TEXT")
  val random = PlainFieldInfo("RANDOM", "INT")
  val dataTemplate = new TableTemplate("BLINGCONSOLESPECIFICATION_DATA", Seq(id, name, random))

  val tag = PrimaryFieldInfo("TAG", "TEXT")
  val tagsTemplate = new TagTableTemplate("BLINGCONSOLESPECIFICATION_TAGS", tag.fieldName)

  val dataHandler = new DataHandler(dataTemplate, tagsTemplate)

  val aliceData = TaggedData(Map("NAME"->"alice", "RANDOM"->4), Set("BLINGCONSOLESPECIFICATION_lol", "BLINGCONSOLESPECIFICATION_rofl"))
  val bobData = TaggedData(Map("NAME"->"bob", "RANDOM"->42), Set("BLINGCONSOLESPECIFICATION_lol"))
  dataHandler.insert(aliceData)
  dataHandler.insert(bobData)

  val tagDag = TagDag("global")

  val console = new BlingConsole with JsonTagDagSerializer {
    type RawData = TaggedData
    type BlingData = Map[String, Any]

    def convertToTaggedData(data: RawData) = data
    def convertToBlingData(data: Map[String, Any]) = data

    val dataHandler = BlingConsoleSpecification.this.dataHandler
    var tagDag = BlingConsoleSpecification.this.tagDag

    val tagDagFileName = "tagDag.txt"
    val blingId = BlingConsoleSpecification.this.blingId
  }

  def afterAll = {
    sql"${dataTemplate.sqlDrop}".execute.apply()
    sql"${tagsTemplate.sqlDrop}".execute.apply()
    dataHandler.tagIndexers.foreach(pair => sql"${pair._2.sqlDrop}".execute.apply())
  }

 "BlingConsole is the only point of contact between bling and its users. As such, it exposes a variety of functionality." >> {
   "The loadData method loads data into the database." >> {
     pending
   }
 }
}
