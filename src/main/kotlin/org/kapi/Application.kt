package org.kapi

import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.kapi.plugins.*
import org.kapi.serializer.ObjectIdJsonSerializer
import java.time.Duration

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
//        todo: move these to environment variables
        val secretEncryptKey = hex("00112233445566778899aabbccddeeff")
        val secretSignKey = hex("6819b57a326945c1968f45236589")
        cookie<JwtSession>("jwt-token") {
            cookie.httpOnly = true
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json {
            serializersModule = SerializersModule {
                contextual(ObjectIdJsonSerializer)
            }
        })
    }

    configureSecurity()
    configureRouting()
    configureUsersRouting()
    configureLobbiesRouting()
    configureGamesRouting()
    configureTagsRouting()
    configureChatRouting()
}
