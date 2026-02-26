package hotkitchen.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Profile(
    val name: String,
    val userType: String,
    val phone : String,
    val email: String,
    val address : String

)

object Profiles : Table() {
    val email = varchar("email", 50).uniqueIndex()
    val name = varchar("name", 30)
    val userType = varchar("userType", 30)
    val phone = varchar("phone", 20)
    val address = varchar("address", 255)

}
