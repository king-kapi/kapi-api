package org.kapi.auth

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.kapi.plugins.JwtSession

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