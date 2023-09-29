package org.kapi.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kapi.auth.registerDiscordAuth
import org.kapi.auth.registerEmailAuth
import org.kapi.auth.registerGoogleAuth
import org.kapi.responses.MessageResponse

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureSecurity() {
    // TODO: Please read the jwt property from the config file if you are using EngineMain
    // https://ktor.io/docs/jwt.html#jwt-settings
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    authentication {
        jwt("auth-jwt") {
            authHeader {
                val jwt = it.sessions.get<JwtSession>()?.jwt ?: return@authHeader null

                parseAuthorizationHeader("Bearer $jwt")
            }
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }

    routing {
        route("/api/auth/sign-out") {
            handle {
                call.sessions.clear("jwt-token")
                call.respond(MessageResponse("Signed out."))
            }
        }
    }

    registerGoogleAuth(httpClient)
    registerDiscordAuth(httpClient)
    // todo: only email if env is development
    registerEmailAuth()
}

@Serializable
data class JwtSession(
    val jwt: String
)

@Serializable
data class LoginStatus(
    val loggedIn: Boolean
)