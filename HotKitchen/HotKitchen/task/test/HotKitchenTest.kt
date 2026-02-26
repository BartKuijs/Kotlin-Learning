import internal.KtorTest
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.testcase.CheckResult

class HotKitchenTest : KtorTest<Any>() {
    @Serializable
    private data class Credentials(var email: String, var userType: String, var password: String)

    @Serializable
    private data class User(
        val name: String, val userType: String, val phone: String, val email: String, val address: String
    )

    @Serializable
    private data class Token(val token: String)

    private val time = System.currentTimeMillis().toString()
    private val jwtRegex = """^[a-zA-Z0-9]+?\.[a-zA-Z0-9]+?\..+""".toRegex()
    private val currentCredentials = Credentials("$time@mail.com", "client", "password$time")
    private var currentUser = User(time + "name", "client", "+79999999999", currentCredentials.email, time + "address")
    private lateinit var signUpToken: String

    @DynamicTest(order = 1)
    fun getSignUpJWTToken(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.post("/signup") {
                    contentType(ContentType.Application.Json)
                    setBody(currentCredentials)
                }
                try {
                    signUpToken = response.body<Token>().token
                    if (!signUpToken.matches(jwtRegex) || signUpToken.contains(currentCredentials.email))
                        result = CheckResult.wrong("Invalid JWT token. (POST /signup)")
                } catch (_: Exception) {
                    result = CheckResult.wrong("Cannot get token. (POST /signup)")
                }
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 2)
    fun correctValidation(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.get("/validate") {
                    bearerAuth(signUpToken)
                }
                if (response.status != HttpStatusCode.OK
                    || response.bodyAsText() != "Hello, ${currentCredentials.userType} ${currentCredentials.email}"
                )
                    result = CheckResult.wrong(
                        "Token validation failed with signin token. (GET /validate)\n" +
                                "Expected: status=200, body='Hello, ${currentCredentials.userType} ${currentCredentials.email}'.\n" +
                                "Actual: status=${response.status.value}, body='${response.bodyAsText()}'."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 3)
    fun getNonExistentUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.get("/me") {
                    bearerAuth(signUpToken)
                }
                if (response.status != HttpStatusCode.BadRequest)
                    result = CheckResult.wrong(
                        "Wrong status code when getting a non-existent user. (GET /me)\n" +
                                "Expected: status=400.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 4)
    fun createUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.put("/me") {
                    bearerAuth(signUpToken)
                    contentType(ContentType.Application.Json)
                    setBody(currentUser)
                }
                if (response.status != HttpStatusCode.OK)
                    result = CheckResult.wrong(
                        "Wrong status code when adding user information by PUT method. (PUT /me)\n" +
                                "Expected: status=200.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 5)
    fun getExistentUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.get("/me") {
                    bearerAuth(signUpToken)
                }
                if (response.status != HttpStatusCode.OK) {
                    result = CheckResult.wrong(
                        "Wrong status code for getting an existing user's information " +
                                "added previously using the PUT method. (GET /me)\n" +
                                "Expected: status=200.\n" +
                                "Actual: status=${response.status.value}."
                    )
                    return@testKtorApplication
                }
                val user: User = response.body()
                if (user != currentUser)
                    result = CheckResult.wrong("(GET /me) returned incorrect user information.")
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 6)
    fun putDifferentEmail(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.put("/me") {
                    val newUser = currentUser.copy(email = "different@mail.com")
                    bearerAuth(signUpToken)
                    contentType(ContentType.Application.Json)
                    setBody(newUser)
                }
                if (response.status != HttpStatusCode.BadRequest)
                    result = CheckResult.wrong(
                        "You're not allowed tp change the user's email! (PUT /me)\n" +
                                "Expected: status=400.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 7)
    fun updateCurrentUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.put("/me") {
                    currentUser =
                        currentUser.copy(name = "newName$time", userType = "newType", address = "newAddress$time")
                    bearerAuth(signUpToken)
                    contentType(ContentType.Application.Json)
                    setBody(currentUser)
                }
                if (response.status != HttpStatusCode.OK)
                    result = CheckResult.wrong(
                        "Cannot update user information by put method. (PUT /me)\n" +
                                "Expected: status=200.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 8)
    fun getNewExistentUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.get("/me") {
                    bearerAuth(signUpToken)
                }
                if (response.status != HttpStatusCode.OK) {
                    result = CheckResult.wrong(
                        "Wrong status code for getting an existing user. (GET /me)\n" +
                                "Expected: status=200.\n" +
                                "Actual: status=${response.status.value}."
                    )
                    return@testKtorApplication
                }
                val user: User = response.body()
                if (user != currentUser)
                    result =
                        CheckResult.wrong("(GET /me) responded with different user information after updating user info.")
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 9)
    fun deleteExistentUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.delete("/me") {
                    bearerAuth(signUpToken)
                }
                if (response.status != HttpStatusCode.OK)
                    result = CheckResult.wrong(
                        "Wrong status code for deleting an existing user. (DELETE /me)\n" +
                                "Expected: status=200.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 10)
    fun deleteNonExistentUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.delete("/me") {
                    bearerAuth(signUpToken)
                }
                if (response.status != HttpStatusCode.NotFound)
                    result = CheckResult.wrong(
                        "Wrong status code for deleting a non-existing user. (DELETE /me)\n" +
                                "Expected: status=404.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 11)
    fun getDeletedUser(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.get("/me") {
                    bearerAuth(signUpToken)
                }
                if (response.status != HttpStatusCode.BadRequest)
                    result = CheckResult.wrong(
                        "Wrong status code for getting a deleted user. (GET /me)\n" +
                                "Expected: status=400.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }

    @DynamicTest(order = 12)
    fun checkDeletedCredentials(): CheckResult {
        var result = CheckResult.correct()
        try {
            testKtorApplication {
                val response = client.post("/signup") {
                    contentType(ContentType.Application.Json)
                    setBody(currentCredentials)
                }
                if (response.status != HttpStatusCode.OK)
                    result = CheckResult.wrong(
                        "Unable to signup after deleting user information. " +
                                "Did you forget to delete user credentials? (POST /signup)\n" +
                                "Expected: status=200.\n" +
                                "Actual: status=${response.status.value}."
                    )
            }
        } catch (e: Exception) {
            result = CheckResult.wrong(e.message)
        }
        return result
    }
}