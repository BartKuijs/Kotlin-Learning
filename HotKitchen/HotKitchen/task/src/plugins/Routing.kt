package hotkitchen.plugins

import hotkitchen.models.Login
import hotkitchen.models.TokenClaim
import hotkitchen.models.TokenConfig
import hotkitchen.models.TokenResponse
import hotkitchen.models.User
import hotkitchen.routes.UserRoutes
import hotkitchen.service.UserService
import hotkitchen.service.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val status: String)



fun Application.configureRouting(tokenConfig : TokenConfig, tokenService : JwtService) {
    UserRoutes(UserService(), tokenService, tokenConfig)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }


    }
}

