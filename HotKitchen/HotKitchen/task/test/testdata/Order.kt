package testdata

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Order(
    var orderId: Int,
    val userEmail: String,
    val mealsIds: List<Int>,
    val price: Float,
    val address: String,
    val status: String
)

data class StrippedOrder(
    val userEmail: String,
    val price: Float,
    val address: String,
    val status: String
) {
    override fun toString(): String = Json.encodeToString(this)
}

fun Order.stripped() = StrippedOrder(userEmail, price, address, status)