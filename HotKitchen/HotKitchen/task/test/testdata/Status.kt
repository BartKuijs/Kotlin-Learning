package testdata

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Status(val status: String) {

    override fun toString(): String = Json.encodeToString(this)

    override fun equals(other: Any?): Boolean =
        this === other || (other is Status && status.equals(other.status, ignoreCase = true))

    override fun hashCode(): Int = status.lowercase().hashCode()
}