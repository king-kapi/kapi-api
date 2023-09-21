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
import org.kapi.data.Tag
import org.kapi.mongo.MongoClientSingleton
import org.kapi.responses.InsertedIdResponse
import org.kapi.responses.MessageResponse

fun Application.configureTagsRouting() {
    val mongoClient = MongoClientSingleton.getClient()

    val database = mongoClient.getDatabase("kapi")
    val collection = database.getCollection<Tag>("tags")

    routing {
        route("/api/tags") {
            get("") {
                val users = collection.find().toList()

                call.respond(users)
            }

            post("") {
                val tag = call.receive<Tag>()

                val result = collection.insertOne(tag)

                if (result.insertedId != null)
                    call.respond(InsertedIdResponse(result.insertedId.asObjectId().value))
                else {
                    call.response.status(HttpStatusCode.InternalServerError)
                    call.respondText("Fail")
                }
            }

            get("/{tagId}") {
                val tagId = call.parameters["tagId"] ?: throw Error("No tagId")

                val tag = collection.find(eq("_id", ObjectId(tagId))).firstOrNull() ?: throw Error("Invalid tagId")

                call.respond(tag)
            }

            delete("/{tagId}") {
                val tagId = call.parameters["tagId"] ?: throw Error("No tagId")

                collection.findOneAndDelete(eq("_id", ObjectId(tagId))) ?: throw Error("Invalid tagId")

                call.respond(MessageResponse("Success"))
            }
        }
    }
}
