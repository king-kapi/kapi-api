package org.kapi.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authenticate("auth-jwt", strategy = AuthenticationStrategy.Optional) {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                if (principal != null) {
                    call.respondText("Hello, ${principal["email"]}!")
                } else {
                    call.respondText("Hello, world!")
                }
            }
        }
    }
}
