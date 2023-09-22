package org.kapi.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kapi.plugins.JwtSession
import org.kapi.responses.MessageResponse

fun Application.registerDiscordAuth(httpClient: HttpClient) {
    authentication {
        oauth("auth-oauth-discord") {
            urlProvider = { "http://localhost:8080/api/auth/discord/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl = "https://discord.com/api/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("DISCORD_CLIENT_ID"),
                    clientSecret = System.getenv("DISCORD_CLIENT_SECRET"),
                    defaultScopes = listOf("email"),
                )
            }
            client = httpClient
        }
    }
    routing {
        authenticate("auth-oauth-discord") {
            get("/api/auth/discord/login") {
                // redirects automagically?
            }

            get("/api/auth/discord/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                val accessToken = principal?.accessToken

                // on success
                if (accessToken != null) {

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
                        call.respondRedirect("/hello")
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

data class DiscordUserInfo(
    val email: String?,
)
