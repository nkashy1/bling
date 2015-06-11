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

  val aliceData = TaggedData(Map("NAME"->"alice", "RANDOM"->4), Set("BLINGCONSOLESPECIFICATION_LOL", "BLINGCONSOLESPECIFICATION_ROFL"))
  val bobData = TaggedData(Map("NAME"->"bob", "RANDOM"->42), Set("BLINGCONSOLESPECIFICATION_LOL"))
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
     val carolData = TaggedData(Map("NAME"->"carol", "RANDOM"->4), Set("BLINGCONSOLESPECIFICATION_NEW"))
     console.loadData(carolData)
     ok
   }

   "The refreshTagDag method refreshes the tagDag member of console so that it reflects the most recent state of the database." >> {
     console.refreshTagDag()
     console.tagDag.tagVertices.keys.toSet mustEqual Set("global", "BLINGCONSOLESPECIFICATION_LOL", "BLINGCONSOLESPECIFICATION_ROFL", "BLINGCONSOLESPECIFICATION_NEW")
   }

   "The extractData method extracts data from the database." >> {
     val selection1 = console.extractData(AllColumns, NoCriterion, "BLINGCONSOLESPECIFICATION_NEW")
     selection1.map(_(console.blingId)).toSet mustEqual Set(3)

     val selection2 = console.extractData(SomeColumns(Seq(blingId)), Gt("RANDOM", 4), "BLINGCONSOLESPECIFICATION_LOL")
     selection2.map(_(console.blingId)).toSet mustEqual Set(2)
   }

   "extractData also accepts as its third, tag argument tags in the tagDag which are not necessarily present in the tags table of the database." >> {
     tagDag.insertTag("laugh")
     tagDag.pushChild("BLINGCONSOLESPECIFICATION_LOL", "laugh")
     tagDag.pushChild("BLINGCONSOLESPECIFICATION_ROFL", "laugh")

     val selection = console.extractData(AllColumns, NoCriterion, "laugh")
     selection.map(_(console.blingId)).toSet mustEqual Set(1, 2)
   }

   "The dataTags methods returns the Set of tags stored in the tags table of the database." >> {
     val dbTags = console.dataTags
     dbTags mustEqual Set("BLINGCONSOLESPECIFICATION_LOL", "BLINGCONSOLESPECIFICATION_ROFL", "BLINGCONSOLESPECIFICATION_NEW")
   }

   "The saveTagDag method allows the current tagDag to be saved to a specified file." >> {
     pending
   }

   "The loadTagDag method allows the current tagDag to be replaced by the TagDag stored in the specified file." >> {
     pending
   }
 }
}
