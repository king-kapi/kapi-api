package org.kapi.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import org.kapi.data.OnboardingParams
import org.kapi.exceptions.UserNotFound
import org.kapi.mongo.MongoClientSingleton
import org.kapi.responses.MessageResponse
import org.kapi.service.UserService

fun Application.configureUsersRouting() {
    val userService = UserService(MongoClientSingleton.getKapiDatabase(environment))

    routing {
        route("/api/users") {
            authenticate("auth-jwt", strategy = AuthenticationStrategy.Required) {
                get("") {
                    call.respond(userService.getAllUsers())
                }

                get("/{userId}") {
                    val userId = ObjectId(call.parameters["userId"])

                    try {
                        val user = userService.getUser(userId)
                        call.respond(user)
                    } catch (e: UserNotFound) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(MessageResponse("Can't find user $userId"))
                    }
                }

                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!["id"]

                    call.respondRedirect("/api/users/$userId")
                }

                get("/me/friends") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = ObjectId(principal!!["id"])

                    try {
                        val friends = userService.getFriends(userId)
                        call.respond(friends)
                    } catch (e: UserNotFound) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(MessageResponse(e.message))
                    }
                }

                post("/me/onboard") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])
                    val params = call.receive<OnboardingParams>()

                    userService.onboardUser(meId, params)
                    call.respond(userService.getUser(meId))
                }
            }
        }
    }
}
