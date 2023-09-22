package org.kapi.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.firstOrNull
import org.kapi.data.User
import org.kapi.mongo.MongoClientSingleton
import java.util.*

class Jwt {
    companion object {
        private val mongoClient = MongoClientSingleton.getClient()
        private val database = mongoClient.getDatabase("kapi")
        private val collection = database.getCollection<User>("users");

        private val jwtAudience = "jwt-audience"
        private val jwtDomain = "https://jwt-provider-domain/"
        private val jwtRealm = "ktor sample app"
        private val jwtSecret = "secret"

        suspend fun create(email: String): String {
            val user = collection.find(Filters.eq("email", email)).firstOrNull()

            // create new user
            if (user == null) {
                throw Exception("Can't find user's email: $email")
            }

            return JWT.create()
                .withAudience(jwtAudience)
                .withIssuer(jwtDomain)
                .withClaim("email", email)
                .withClaim("id", user.id.toString())
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256(jwtSecret));
        }
    }
}