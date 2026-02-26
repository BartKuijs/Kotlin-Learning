package hotkitchen.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import hotkitchen.models.TokenConfig
import hotkitchen.models.TokenClaim
import java.util.Date

class JwtService {
    fun generate(config: TokenConfig, vararg claims: TokenClaim): String {
        val token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis()+config.expiresIn))
        // trailing Lambda
        claims.forEach { claim ->
            token.withClaim(claim.name, claim.value)
        }
        return token.sign(Algorithm.HMAC256(config.secret))
    }
}