package hotkitchen.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Expression

@Serializable
data class User(
    val email: String,
    val userType: String,
    val password: String
)

object Users : Table() {
    val email = varchar("email", 30).uniqueIndex()
    val userType = varchar("userType", 30)
    val password = varchar("password", 30)

    override fun toString() : String{
        return "$email, $userType, $password"
    }
}

