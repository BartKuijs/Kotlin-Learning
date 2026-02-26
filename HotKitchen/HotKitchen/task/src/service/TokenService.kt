package hotkitchen.service

import hotkitchen.models.TokenConfig
import hotkitchen.models.TokenClaim

interface TokenService {
    fun generate(
        config: TokenConfig,
        vararg claims: TokenClaim
    ) : String
}