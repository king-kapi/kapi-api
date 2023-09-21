package org.kapi

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.kapi.plugins.*
import org.kapi.serializer.ObjectIdJsonSerializer

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            serializersModule = SerializersModule {
                contextual(ObjectIdJsonSerializer)
            }
        })
    }

    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    configureSecurity()
    configureRouting()
    configureUsersRouting()
    configureGamesRouting()
    configureTagsRouting()
}
