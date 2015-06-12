package systems.adaptix.bling

import collection.JavaConversions._
import io.StdIn._

import com.typesafe.config._
import scalikejdbc._
import scalikejdbc.config._

import data._
import tags._

/**
 * Created by nkashyap on 6/11/15.
 */
object SampleApp extends App {
  val conf = ConfigFactory.load()
  val consoleConfig = conf.getConfig("console")

  val blingId = consoleConfig.getString("blingId")
  val tagDagFileName = consoleConfig.getString("tagDagFile")

  val dataTableConfig = consoleConfig.getConfig("dataSchema")
  val dataTableName = dataTableConfig.getString("tableName")
  val dataTableColumns = dataTableConfig.getStringList("columns")
  val dataTableColumnTypes = dataTableColumns.map(column => dataTableConfig.getString(column))
  val dataFields = (dataTableColumns zip dataTableColumnTypes).map( fieldSpec => PlainFieldInfo(fieldSpec._1, fieldSpec._2) )
  val idField = AutoIdFieldInfo(blingId)
  val dataTemplate = new TableTemplate(dataTableName, Seq[FieldInfo](idField) ++ dataFields)

  val tagsTableConfig = consoleConfig.getConfig("tagsSchema")
  val tagsTableName = tagsTableConfig.getString("tableName")
  val tagsTableColumnName = tagsTableConfig.getString("column")
  val tagsTemplate = new TagTableTemplate(tagsTableName, tagsTableColumnName)

  DBs.setupAll()
  implicit val session = AutoSession

  val dataHandler = new DataHandler(dataTemplate, tagsTemplate)
  val tagDag = TagDag("global")

  val console = new BlingConsole with JsonTagDagSerializer {
    type RawData = TaggedData
    type BlingData = Map[String, Any]

    def convertToTaggedData(data: RawData) = data
    def convertToBlingData(data: Map[String, Any]) = data

    val dataHandler = SampleApp.this.dataHandler
    var tagDag = SampleApp.this.tagDag

    val tagDagFileName = SampleApp.this.tagDagFileName
    val blingId = SampleApp.this.blingId
  }

  def runLoop: Unit = {
    while (true) {
      val choice = readLine(
        """
          |Choose your option:
          |1. Insert data
          |2. Extract data
          |3. Exit
        """.stripMargin)
      choice match {
        case "1" => insertData
        case "2" => extractData
        case "3" => return
        case _ => println("Sorry, invalid option. Please select again.")
      }
    }
  }

  def insertData = {
    val dataValues = console.dataHandler.dataTemplate.columns.
      filter( _.fieldName != console.blingId ).
      map( column => readLine("%s: ", column.fieldName) )
    val dataKeys = console.dataHandler.dataTemplate.columns.map( _.fieldName ).filter( _ != console.blingId )
    val dataMap = Map( (dataKeys zip dataValues):_* )
    val tags = readLine("Insert tags separated by commas: ").split(",")
    console.loadData(TaggedData(dataMap, tags.toSet))
  }

  def extractData = {
    val tag = readLine("Please enter tag you want to extract data for: ")
    console.refreshTagDag()
    println(console.extractData(AllColumns, NoCriterion, tag))
  }

  runLoop
}
