package internal

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.test.TestResult
import kotlinx.serialization.json.Json
import org.hyperskill.hstest.stage.StageTest

abstract class KtorTest<T> : StageTest<T>() {
    fun testKtorApplication(
        configPath: String = "application.yaml",
        block: suspend ApplicationTestBuilder.() -> Unit
    ): TestResult = testApplication {
        environment {
            config = ApplicationConfig(configPath)
        }

        client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        block()
    }

    suspend inline fun <reified T> HttpResponse.safeBody(contextInfo: String = "response body"): T =
        try {
            body<T>()
        } catch (_: JsonConvertException) {
            throw RuntimeException(
                "Failed to parse $contextInfo.\n" +
                        "Received body: '${bodyAsText()}'.\n" +
                        "Incorrect JSON structure or missing fields (fields are case-sensitive)."
            )
        }
}