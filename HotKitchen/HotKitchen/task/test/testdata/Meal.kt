package testdata

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Meal(
    val mealId: Int,
    val title: String,
    val price: Float,
    val imageUrl: String,
    val categoryIds: List<Int>
) {
    override fun toString(): String = Json.encodeToString(this)
}