package org.kapi.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kapi.mongo.MongoClientSingleton
import org.kapi.plugins.JwtSession
import org.kapi.responses.MessageResponse
import org.kapi.service.UserService

fun Application.registerGoogleAuth(httpClient: HttpClient) {
    val jwt = Jwt(UserService(MongoClientSingleton.getKapiDatabase(environment)))

    val client_id = environment.config.property("ktor.deployment.google_client_id").getString()
    val secret = environment.config.property("ktor.deployment.google_secret").getString()

    routing {
        get("/api/auth/google/exchange") {
            val code = call.request.queryParameters["code"]
            val redirectUri = call.request.queryParameters["redirect_uri"]
            if (code == null) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(MessageResponse("Missing code."))
            } else if (redirectUri == null) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(MessageResponse("Missing redirect_uri."))
            } else {
                val response = httpClient.submitForm(
                    url = "https://oauth2.googleapis.com/token",
                    formParameters = parameters {
                        append("client_id", client_id)
                        append("client_secret", secret)
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("redirect_uri", redirectUri)
                    }
                )

                println("Got ${response.body<String>()}");

                if (response.status.value in 200..299) {
                    val tokenInfo: GoogleTokenInfo = response.body()
                    val accessToken = tokenInfo.accessToken

                    val userInfo: UserInfo = httpClient.get("https://www.googleapis.com/userinfo/v2/me") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $accessToken")
                        }
                    }.body()

                    val token = jwt.create(userInfo.email)
                    call.sessions.set(JwtSession(token))
                    call.respond("")
                } else {
                    call.response.status(HttpStatusCode.Unauthorized)
                    call.respond(MessageResponse("Unauthorized."))
                }
            }
        }
    }
}


@Serializable
data class GoogleTokenInfo(
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("id_token")
    val idToken: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    val scope: String
)

@Serializable
data class UserInfo(
    val id: String,
    val email: String,
    @SerialName("verified_email")
    val verified: Boolean,
    val name: String,
    val picture: String,
)
