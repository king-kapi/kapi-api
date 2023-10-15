package org.kapi.service

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.kapi.data.Lobby
import org.kapi.data.LobbyRequest
import org.kapi.exceptions.LobbyNotFound

class LobbyService(database: MongoDatabase) {
    private val collection = database.getCollection<Lobby>("lobbies")

    fun getCollection(): MongoCollection<Lobby> {
        return collection
    }

    suspend fun getAllLobbies(): List<Lobby> {
        return collection.find().toList()
    }

    suspend fun createLobby(lobby: Lobby): Lobby {
        val insertedId = collection.insertOne(lobby).insertedId!!.asObjectId()
        return collection.find(eq("_id", insertedId)).first()
    }

    suspend fun getLobby(lobbyId: ObjectId): Lobby? {
        return collection.find(eq("_id", lobbyId)).firstOrNull()
    }

    suspend fun sendJoinRequest(lobbyId: ObjectId, senderId: ObjectId, message: String = ""): Lobby? {
        // verify lobby exists
        getLobby(lobbyId) ?: throw LobbyNotFound(lobbyId)

        val updates = Updates.push(Lobby::requests.name, LobbyRequest(sender = senderId, message = message))

        return collection.findOneAndUpdate(
            eq("_id", lobbyId),
            updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }

    suspend fun acceptJoinRequest(lobbyId: ObjectId, senderId: ObjectId): Lobby? {
        // verify lobby exists
        getLobby(lobbyId) ?: throw LobbyNotFound(lobbyId)

        val updates =
            Updates.combine(
                Updates.push(Lobby::users.name, senderId),
                Updates.pull(Lobby::requests.name, eq("sender", senderId))
            )

        return collection.findOneAndUpdate(
            eq("_id", lobbyId), updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }

    suspend fun denyJoinRequest(lobbyId: ObjectId, senderId: ObjectId): Lobby? {
        // verify lobby exists
        getLobby(lobbyId) ?: throw LobbyNotFound(lobbyId)

        val updates =
            Updates.combine(
                Updates.pull(Lobby::requests.name, eq("sender", senderId))
            )

        return collection.findOneAndUpdate(
            eq("_id", lobbyId), updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }

    suspend fun kickPlayer(lobbyId: ObjectId, kickedId: ObjectId): Lobby? {
        // verify lobby exists
        getLobby(lobbyId) ?: throw LobbyNotFound(lobbyId)

        val updates =
            Updates.combine(
                Updates.pull(Lobby::users.name, kickedId)
            )

        return collection.findOneAndUpdate(
            eq("_id", lobbyId), updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }
}