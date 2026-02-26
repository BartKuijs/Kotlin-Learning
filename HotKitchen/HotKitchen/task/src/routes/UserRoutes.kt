package hotkitchen.routes

import hotkitchen.models.Login
import hotkitchen.models.Profile
import hotkitchen.models.TokenClaim
import hotkitchen.models.TokenConfig
import hotkitchen.models.TokenResponse
import hotkitchen.models.User
import hotkitchen.plugins.AuthResponse
import hotkitchen.service.JwtService
import hotkitchen.service.TokenService
import hotkitchen.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing


fun Application.UserRoutes(userService: UserService, tokenService: JwtService, tokenConfig: TokenConfig) {
    routing{
        get("/users") {
            val users = userService.getAllUsers()
            call.respond(users)
        }

        post("/signup") {
            val signup = call.receive<User>()
            val users = userService.getAllUsers()

            val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d).+$")
            if (signup.password.length < 6 || !regex.matches(signup.password)) {
                return@post call.respond(HttpStatusCode.Forbidden, AuthResponse("Invalid password"))
            }
            val mailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            if (!mailRegex.matches(signup.email)) {
                return@post call.respond(HttpStatusCode.Forbidden, AuthResponse("Invalid email"))
            }
            for (user in users) {
                if (user == signup.email) {
                    return@post call.respond(HttpStatusCode.Forbidden, AuthResponse("User already exists"))
                }
            }
            userService.writeUserToDB(signup)

            val token = tokenService.generate(
                config = tokenConfig,
                TokenClaim(
                    name = "userEmail",
                    value = signup.email,
                ), TokenClaim(
                    name = "userType",
                    value = signup.userType
                )
            )

            call.respond(HttpStatusCode.OK, message = TokenResponse(token = token))
        }

        post("/signin") {
            val signup = call.receive<Login>()
            val user = userService.getUserByEmail(signup.email)

            if (user == null || user.password != signup.password) {
                return@post call.respond(HttpStatusCode.Forbidden, AuthResponse("Invalid email or password"))
            } else {
                val token = tokenService.generate(
                    config = tokenConfig,
                    TokenClaim(
                        name = "userEmail",
                        value = user.email,
                    ), TokenClaim(
                        name = "userType",
                        value = user.userType
                    )
                )
                call.respond(HttpStatusCode.OK, message = TokenResponse(token = token))
            }
        }

        authenticate {
            get("/validate") {
                val principal = call.principal<JWTPrincipal>()
                val userEmail = principal?.getClaim("userEmail", String::class)
                val userType = principal?.getClaim("userType", String::class)
                call.respond(HttpStatusCode.OK, "Hello, $userType $userEmail")
            }

            get("/me"){
                val principal = call.principal<JWTPrincipal>()
                val userEmail = principal?.getClaim("userEmail", String::class)
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                try{
                    val profile = userService.getProfileByEmail(userEmail)

                    if (profile != null) {
                        call.respond(HttpStatusCode.OK, message = profile)
                    }
                    else{
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }catch (e: Exception){
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            put("/me"){
                try{
                val principal = call.principal<JWTPrincipal>()
                val userEmail = principal?.getClaim("userEmail", String::class)
                    ?: return@put call.respond(HttpStatusCode.BadRequest)

                val profile = call.receive<Profile>()
                if (profile.email != userEmail) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                else {
                    userService.upsertProfileToDB(profile)
                    call.respond(HttpStatusCode.OK, message = profile)
                }}catch (e: Exception){
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }

            }

            delete("/me"){
                val principal = call.principal<JWTPrincipal>()
                val userEmail = principal?.getClaim("userEmail", String::class)
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val user = userService.getUserByEmail(userEmail)
                val profile = userService.getProfileByEmail(userEmail)

                if (profile != null && user != null) {
                    userService.deleteProfileFromDB(user)
                    userService.deleteUserFromDB(user)
                    call.respond(HttpStatusCode.OK)
                }
                else if(user != null){
                    userService.deleteUserFromDB(user)
                    call.respond(HttpStatusCode.OK)
                }
                else{
                    return@delete call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}