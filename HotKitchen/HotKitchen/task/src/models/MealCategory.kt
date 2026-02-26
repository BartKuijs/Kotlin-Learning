package hotkitchen.models

import org.jetbrains.exposed.sql.Table

data class MealCategory(
    val mealId: Int,
    val categoryId: Int
)

object MealCategories : Table() {
    val mealId = integer("mealid").uniqueIndex()
    val categoryId = integer("categoryid").uniqueIndex()
}