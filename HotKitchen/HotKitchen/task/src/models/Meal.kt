package hotkitchen.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Meal(
    val mealId : Int,
    var title : String,
    val price : Float,
    val imageUrl: String,
    var categoryIds : MutableList<Int>
)


object Meals : Table() {
    val mealId = integer("mealid").uniqueIndex()
    val title = varchar("title", 30)
    val price = float("price")
    val imageUrl = varchar("imageurl", 255)
}