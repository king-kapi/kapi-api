package org.kapi.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.kapi.data.User
import org.kapi.data.createNewUser
import org.kapi.mongo.MongoClientSingleton
import java.util.*

class Jwt {
    companion object {
        private val mongoClient = MongoClientSingleton.getClient()
        private val database = mongoClient.getDatabase("kapi")
        private val collection = database.getCollection<User>("users")

        private const val JWT_AUDIENCE = "jwt-audience"
        private const val JWT_DOMAIN = "https://jwt-provider-domain/"

        //        private const val jwtRealm = "ktor sample app"
        private const val JWT_SECRET = "secret"

        suspend fun create(email: String): String {
            var user = collection.find(Filters.eq("email", email)).firstOrNull()

            // create new user
            if (user == null) {
                val result = collection.insertOne(createNewUser(email))
                val insertedId = result.insertedId?.asObjectId()?.value ?: throw Exception("Something is wrong")
                user = collection.find(Filters.eq("_id", insertedId)).first()
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
}