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
        get("/api/users") {
            val users = collection.find().toList()

            call.respond(users);
        }
    }
}
