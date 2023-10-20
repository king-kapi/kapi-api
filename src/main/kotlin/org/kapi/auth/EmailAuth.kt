package org.kapi.auth

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.kapi.mongo.MongoClientSingleton
import org.kapi.plugins.JwtSession
import org.kapi.service.user.UserService

// for development use only
fun Application.registerEmailAuth() {
    val jwt = Jwt(UserService(MongoClientSingleton.getKapiDatabase(environment)))

    routing {
        post("/api/auth/email/login") {
            val email = call.receive<AuthParams>().email

            val token = jwt.create(email)
            call.sessions.set(JwtSession(token))
            call.respond(hashMapOf("token" to token))
        }
    }
}

@Serializable
data class AuthParams(
    val email: String
)