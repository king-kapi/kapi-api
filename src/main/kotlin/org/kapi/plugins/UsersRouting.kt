package org.kapi.plugins

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.kapi.data.MinimalUser
import org.kapi.data.OnboardingParams
import org.kapi.data.User
import org.kapi.data.minimalUserProjection
import org.kapi.mongo.MongoClientSingleton
import org.kapi.responses.MessageResponse

fun Application.configureUsersRouting() {
    val mongoClient = MongoClientSingleton.getClient()

    val database = mongoClient.getDatabase("kapi")
    val collection = database.getCollection<User>("users")

    routing {
        route("/api/users") {
            authenticate("auth-jwt", strategy = AuthenticationStrategy.Required) {
                get("") {
                    val users = collection.find().toList()

                    call.respond(users)
                }

                get("/{userId}") {
                    val userId = call.parameters["userId"]

                    val user = collection.find(eq("_id", ObjectId(userId))).firstOrNull()
                    if (user == null) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(MessageResponse("Can't find user $userId"))
                    } else
                        call.respond(user)
                }

                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!["id"]

                    call.respondRedirect("/api/users/$userId")
                }

                get("/me/friends") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!["id"]

                    val user = collection.find(eq("_id", ObjectId(userId))).firstOrNull()

                    if (user?.friends == null) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(MessageResponse("Can't find friends of user $userId"))
                    } else {
                        val friends = ArrayList<MinimalUser>()
                        for (friendId in user.friends) {
                            val friend =
                                collection.find<MinimalUser>(eq("_id", friendId))
                                    .projection(minimalUserProjection)
                                    .firstOrNull()
                            if (friend != null) {
                                friends.add(friend)
                            }
                        }

                        call.respond(friends)
                    }
                }

                post("/me/onboard") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])
                    val params = call.receive<OnboardingParams>()

                    val update = Updates.combine(
                        Updates.set("username", params.username),
                        Updates.set("games", params.games),
                        Updates.set("pronouns", params.pronouns),
                        Updates.set("birthday", params.birthday),
                        Updates.set("language", params.language),
                        Updates.set("timezone", params.timezone),
                        Updates.set("avatarColor", params.avatarColor),
                        Updates.set("onboarded", true)
                    )

                    val updated = collection.updateOne(eq("_id", meId), update)
                    call.respond(collection.find(eq("_id", meId)).first())
                }
            }
        }
    }
}
