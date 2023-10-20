package org.kapi.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.kapi.service.user.UserService
import java.util.*

class Jwt(private val userService: UserService) {
    private val TWELVE_HOURS = 12 * 60 * 60 * 1000// in milliseconds
    private val JWT_AUDIENCE = "jwt-audience"
    private val JWT_DOMAIN = "https://jwt-provider-domain/"

    //        private const val jwtRealm = "ktor sample app"
    private val JWT_SECRET = "secret"

    suspend fun create(email: String): String {
        val user = try {
            userService.getUser(userEmail = email)
        } catch (e: Exception) {
            // create new user
            userService.createNewUser(email)
        }

        return JWT.create()
            .withAudience(JWT_AUDIENCE)
            .withIssuer(JWT_DOMAIN)
            .withClaim("email", email)
            .withClaim("id", user.id.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + TWELVE_HOURS))
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }
}