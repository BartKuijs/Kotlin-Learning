package hotkitchen.repositories

import hotkitchen.models.Categories
import hotkitchen.models.Category
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepository {
    fun getAllCategories() : List<Category> = transaction {
        Categories.selectAll()
            .map { Category(it[Categories.categoryId], it[Categories.title], it[Categories.description]) }

    }

    fun getCategoryById(categoryId: Int) : Category? = transaction {
        Categories.selectAll()
            .where { Categories.categoryId eq categoryId }
            .map { Category(it[Categories.categoryId], it[Categories.title], it[Categories.description]) }
            .singleOrNull()
    }

    fun addCategory(category: Category) : Unit = transaction {
        Categories.insert {
            it[Categories.categoryId] = category.categoryId
            it[Categories.title] = category.title
            it[Categories.description] = category.description
        }
    }
}