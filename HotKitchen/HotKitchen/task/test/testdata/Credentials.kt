package testdata

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(var email: String, var userType: String, var password: String)

@Serializable
data class SignInCredentials(var email: String, var password: String)

fun Credentials.signInCredentials() = SignInCredentials(email, password)