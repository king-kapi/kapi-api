package org.kapi.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters.eq
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import org.kapi.data.User
import org.kapi.mongo.MongoClientSingleton
import org.kapi.responses.MessageResponse
import java.util.*

val applicationHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

@Serializable
data class AuthParams(
    val email: String
)

fun Application.configureSecurity() {
    val mongoClient = MongoClientSingleton.getClient()
    val database = mongoClient.getDatabase("kapi")
    val collection = database.getCollection<User>("users");

    // TODO: Please read the jwt property from the config file if you are using EngineMain
    // https://ktor.io/docs/jwt.html#jwt-settings
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    authentication {
        jwt("auth-jwt") {
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

    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile"),
                )
            }
            client = applicationHttpClient
        }
    }
    routing {

        post("/api/auth") {
            val email = call.receive<AuthParams>().email

            val user = collection.find(eq("email", email)).firstOrNull()

//            call.response.status(HttpStatusCode.BadRequest)
//            call.respond(MessageResponse("Invalid"))

            val token = JWT.create()
                .withAudience(jwtAudience)
                .withIssuer(jwtDomain)
                .withClaim("email", email)
                .withClaim("id", user?.id.toString())
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256(jwtSecret))

            call.respond(hashMapOf("token" to token))
        }

        authenticate("auth-jwt") {
            get("/api/auth") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                val id = principal.payload.getClaim("id").asString()

                call.respond(MessageResponse("Signed in with user $email ($id)"))
            }
        }

//        authenticate("auth-oauth-google") {
//            get("login") {
//                call.respondRedirect("/callback")
//            }
//
//            get("/callback") {
//                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
//                val accessToken = principal?.accessToken.toString()
//                call.sessions.set(UserSession(accessToken))
//                call.respondRedirect("/")
//            }
//        }
    }
}

@Serializable
data class UserSession(val accessToken: String)
