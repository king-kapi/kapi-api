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
import org.kapi.plugins.JwtSession
import org.kapi.plugins.UserInfo
import org.kapi.responses.MessageResponse

fun Application.registerGoogleAuth(httpClient: HttpClient) {
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
        authenticate("auth-oauth-google") {
            get("/api/auth/google/login") {
                // redirects automagically?
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                val accessToken = principal?.accessToken

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
        }
    }
}