package hotkitchen.repositories

import hotkitchen.models.Meal
import hotkitchen.models.MealCategories
import hotkitchen.models.Meals
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class MealRepository {
    fun getAllMeals() : List<Meal> = transaction {
        val meals =  Meals.selectAll()
            .map { Meal(it[Meals.mealId], it[Meals.title], it[Meals.price], it[Meals.imageUrl], mutableListOf()) };

        for (meal in meals) {
            val categories =  MealCategories.selectAll().where(MealCategories.mealId eq meal.mealId)
            meal.categoryIds.addAll(categories.map { it[MealCategories.categoryId] })
        }

        meals
    }

    fun getMealById(mealId: Int) : Meal? = transaction {
        val meal = Meals.selectAll()
            .where { Meals.mealId eq mealId }
            .map { Meal(it[Meals.mealId], it[Meals.title], it[Meals.price], it[Meals.imageUrl], mutableListOf()) }
            .singleOrNull()

        if (meal != null) {
            val categories =  MealCategories.selectAll().where(MealCategories.mealId eq meal.mealId)
            meal.categoryIds.addAll(categories.map { it[MealCategories.categoryId] })
        }

        meal
    }

    fun addMeal(meal: Meal) : Unit = transaction {
        Meals.insert {
            it[Meals.mealId] = meal.mealId
            it[Meals.title] = meal.title
            it[Meals.price] = meal.price
            it[Meals.imageUrl] = meal.imageUrl
        }

        for (category in meal.categoryIds) {
            MealCategories.insert {
                it[MealCategories.mealId] = meal.mealId
                it[MealCategories.categoryId] = category
            }
        }
    }
}