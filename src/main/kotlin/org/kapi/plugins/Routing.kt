package org.kapi.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureRouting() {
    routing {
        get("/") {
            val userSession: UserSession? = call.sessions.get()
            if (userSession != null) {
                call.respondText("Hello, World!.")
            } else {
                call.respondText("Not Authenticated.")
            }
        }
    }
}
