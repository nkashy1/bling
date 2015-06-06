package systems.adaptix.bling.data

/**
 * Created by nkashyap on 6/5/15.
 */
class TagTableTemplate(override val tableName: String, val columnName: String) extends TableTemplate(tableName, Seq( PrimaryFieldInfo(columnName, "VARCHAR") ))
