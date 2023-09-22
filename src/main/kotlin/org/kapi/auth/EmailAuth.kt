package org.kapi.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import org.kapi.data.User
import org.kapi.mongo.MongoClientSingleton
import org.kapi.plugins.JwtSession
import org.kapi.responses.MessageResponse
import java.util.*

// for development use only
fun Application.registerEmailAuth() {
    routing {

        post("/api/auth/email/login") {
            val email = call.receive<AuthParams>().email

            val token = Jwt.create(email)
            call.sessions.set(JwtSession(token))
            call.respond(hashMapOf("token" to token))
        }
    }
}

@Serializable
data class AuthParams(
    val email: String
)