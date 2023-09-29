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
import org.kapi.plugins.JwtSession
import org.kapi.responses.MessageResponse

fun Application.registerDiscordAuth(httpClient: HttpClient) {
    val client_id = environment.config.property("ktor.deployment.discord_client_id").getString()
    val secret = environment.config.property("ktor.deployment.discord_secret").getString()

    routing {
        get("/api/auth/discord/exchange") {
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
                    url = "https://discord.com/api/oauth2/token",
                    formParameters = parameters {
                        append("client_id", client_id)
                        append("client_secret", secret)
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("redirect_uri", redirectUri)
                    }
                )

                if (response.status.value in 200..299) {
                    val tokenInfo: DiscordTokenInfo = response.body()
                    val accessToken = tokenInfo.accessToken

                    val userInfo: DiscordUserInfo = httpClient.get("https://discord.com/api/users/@me") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $accessToken")
                        }
                    }.body()

                    if (userInfo.email == null) {
                        call.response.status(HttpStatusCode.BadRequest)
                        call.respond(MessageResponse("No email in discord account."))
                    } else {
                        val token = Jwt.create(userInfo.email)
                        call.sessions.set(JwtSession(token))
                        call.respond("")
                    }
                } else {
                    call.response.status(HttpStatusCode.Unauthorized)
                    call.respond(MessageResponse("Unauthorized."))
                }
            }
        }
    }
}

@Serializable
data class DiscordTokenInfo(
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("refresh_token")
    val refreshToken: String,
    val scope: String
)

@Serializable
data class DiscordUserInfo(
    val email: String?,
)
