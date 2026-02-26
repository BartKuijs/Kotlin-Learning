package hotkitchen.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Category(
    val categoryId: Int,
    val title: String,
    val description: String
)

object Categories : Table() {
    val categoryId = integer("categoryid").uniqueIndex()
    val title = varchar("title", 30)
    val description = varchar("description", 255)
}
