package org.kapi.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters.eq
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kapi.auth.Jwt
import org.kapi.data.User
import org.kapi.mongo.MongoClientSingleton
import org.kapi.responses.MessageResponse
import java.util.*

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

@Serializable
data class AuthParams(
    val email: String?
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
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.email"),
                )
            }
            client = httpClient
        }
    }
    routing {
        route("/api/auth/signout") {
            handle {
                call.sessions.clear("jwt-token")
                call.respond(MessageResponse("Signed out."))
            }
        }


        authenticate("auth-oauth-google") {
            get("/api/auth/google/login") {
                // redirects automagically?
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                val accessToken = principal?.accessToken;

                // on success
                if (accessToken != null) {

                    val userInfo: UserInfo = httpClient.get("https://www.googleapis.com/userinfo/v2/me") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $accessToken")
                        }
                    }.body()

                    val token = Jwt.create(userInfo.email)
                    call.sessions.set(JwtSession(token))
                    call.respondRedirect("/hello")
                } else {
                    call.response.status(HttpStatusCode.Unauthorized)
                    call.respond(MessageResponse("Unauthorized."))
                }
            }

            post("/api/auth") {
                val email = call.receive<AuthParams>().email

                // verify user
                // check google
                val user = collection.find(eq("email", email)).firstOrNull()

                // test not verified
                if (true) {
                    call.response.status(HttpStatusCode.Unauthorized)
                    call.respond(MessageResponse("Not Authorized"))
                }

                val token = JWT.create()
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .withClaim("email", email)
                    .withClaim("id", user?.id.toString())
                    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                    .sign(Algorithm.HMAC256(jwtSecret))

                call.respond(hashMapOf("token" to token))
            }
        }
    }
}

@Serializable
data class UserInfo(
    val id: String,
    val email: String,
    @SerialName("verified_email")
    val verified: Boolean,
    val name: String,
    val picture: String,
)

@Serializable
data class JwtSession(
    val jwt: String
)
