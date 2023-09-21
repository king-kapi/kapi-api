package org.kapi.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.kapi.data.User
import org.kapi.mongo.MongoClientSingleton

fun Application.configureUsersRouting() {
    val mongoClient = MongoClientSingleton.getClient()

    val database = mongoClient.getDatabase("kapi");
    val collection = database.getCollection<User>("users");

    routing {
        route("/api/users") {
            get("/") {
                val users = collection.find().toList()

                call.respond(users);
            }

            get("/current") {
                call.respondText("stub");
            }

            get("/{userId}") {
                val userId = call.parameters["userId"];
                if (userId != null)
                    call.respondText(userId)
                call.respond("stub")
            }

            get("/{userId}/status") {
                val userId = call.parameters["userId"];
                if (userId != null)
                    call.respondText(userId)
                call.respond("stub")
            }

            get("/status") {
                call.respondText("stub");
            }

            get("/friends") {
                call.respondText("stub");
            }

            post("/friends") {
                call.respondText("stub");
            }

            post("/onboard") {
                call.respondText("stub");
            }
        }
    }
}
