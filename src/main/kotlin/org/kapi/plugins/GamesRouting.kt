package org.kapi.plugins

import com.mongodb.client.model.Filters.eq
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.kapi.data.Game
import org.kapi.mongo.MongoClientSingleton
import org.kapi.responses.InsertedIdResponse
import org.kapi.responses.MessageResponse

fun Application.configureGamesRouting() {
    val mongoClient = MongoClientSingleton.getClient()

    val database = mongoClient.getDatabase("kapi")
    val collection = database.getCollection<Game>("games")

    routing {
        route("/api/games") {
            get("") {
                val users = collection.find().toList()

                call.respond(users)
            }

            post("") {
                val game = call.receive<Game>()

                val result = collection.insertOne(game)

                if (result.insertedId != null)
                    call.respond(InsertedIdResponse(result.insertedId.asObjectId().value))
                else {
                    call.response.status(HttpStatusCode.InternalServerError)
                    call.respondText("Fail")
                }
            }

            get("/{gameId}") {
                val gameId = call.parameters["gameId"] ?: throw Error("No gameId")

                val game = collection.find(eq("_id", ObjectId(gameId))).firstOrNull() ?: throw Error("Invalid gameId")

                call.respond(game)
            }

            delete("/{gameId}") {
                val gameId = call.parameters["gameId"] ?: throw Error("No gameId")

                collection.findOneAndDelete(eq("_id", ObjectId(gameId))) ?: throw Error("Invalid gameId")

                call.respond(MessageResponse("Success"))
            }
        }
    }
}
