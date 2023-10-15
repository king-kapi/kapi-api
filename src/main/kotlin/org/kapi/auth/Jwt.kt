package org.kapi.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.kapi.data.User
import org.kapi.data.createNewUser
import org.kapi.mongo.MongoClientSingleton
import org.kapi.service.UserService
import java.util.*

class Jwt(private val userService: UserService) {
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
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }
}