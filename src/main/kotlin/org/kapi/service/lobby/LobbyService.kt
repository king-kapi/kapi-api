package org.kapi.service.lobby

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.kapi.data.Chat
import org.kapi.data.LobbyDto
import org.kapi.data.LobbyRequestDto
import org.kapi.data.User
import org.kapi.exceptions.LobbyNotFound
import org.kapi.exceptions.LobbyRequestNotFound
import org.kapi.service.chat.ChatService
import org.kapi.service.user.MinimalUser
import org.kapi.service.user.UserService

class LobbyService(database: MongoDatabase) {
    private val collection = database.getCollection<LobbyDto>("lobbies")
    private val userService = UserService(database)
    private val chatService = ChatService(database)

    fun getCollection(): MongoCollection<LobbyDto> {
        return collection
    }

    suspend fun getAllLobbies(): List<LobbyDto> {
        return collection.find().toList()
    }

    suspend fun getLobby(lobbyId: ObjectId): LobbyDto {
        val lobbyDto = collection.find(eq("_id", lobbyId)).firstOrNull() ?: throw LobbyNotFound(lobbyId);

        return lobbyDto;
//        val users = ArrayList<User>()
//        for (userId in lobbyDto.users) {
//            users.add(userService.getUser(userId))
//        }
//
//        val requestsWithUser = ArrayList<LobbyRequestWithUser>()
//        for (request in lobbyDto.requests) {
//            requestsWithUser.add(
//                LobbyRequestWithUser(
//                    id = request.id,
//                    sender = MinimalUser(userService.getUser(request.sender)),
//                    message = request.message
//                )
//            )
//        }
//
//        return FullLobby(
//            id = lobbyDto.id,
//            hostId = lobbyDto.hostId,
//            name = lobbyDto.name,
//            game = lobbyDto.game,
//            tags = lobbyDto.tags,
//            numPlayers = lobbyDto.numPlayers,
//            description = lobbyDto.description,
//            users = users,
//            chatId = lobbyDto.chatId,
//            requests = requestsWithUser
//        );
    }

    suspend fun createLobby(lobby: LobbyDto): LobbyDto {
        val insertedId = collection.insertOne(lobby).insertedId!!.asObjectId().value

        // create a chat
        val chat = chatService.createChat(
            Chat(
                name = "lobby-${insertedId}",
                users = lobby.users
            )
        );

        val update = Updates.set(LobbyDto::chatId.name, chat.id)
        collection.findOneAndUpdate(
            eq("_id", insertedId),
            update,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )

        return getLobby(insertedId)
    }

    suspend fun deleteLobby(lobbyId: ObjectId): Boolean {
        return try {
            collection.deleteOne(eq("_id", lobbyId))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendJoinRequest(lobbyId: ObjectId, senderId: ObjectId, message: String = ""): LobbyRequestDto {
        // verify lobby exists
        getLobby(lobbyId)

        val newRequest = LobbyRequestDto(sender = senderId, message = message)

        val updates =
            Updates.push(LobbyDto::requests.name, newRequest)

        collection.updateOne(eq("_id", lobbyId), updates)

        return newRequest;
    }

    suspend fun acceptJoinRequest(lobbyId: ObjectId, requestId: ObjectId): LobbyDto? {
        // verify lobby exists
        val lobby = getLobby(lobbyId)

        // find sender
        var sender: ObjectId? = null
        for (request in lobby.requests) {
            if (request.id == requestId)
                sender = request.sender
        }

        if (sender == null)
            throw LobbyRequestNotFound(lobbyId, requestId)

        val updates =
            Updates.combine(
                Updates.push(LobbyDto::users.name, sender),
                Updates.pull(LobbyDto::requests.name, eq("_id", requestId))
            )

        return collection.findOneAndUpdate(
            eq("_id", lobbyId), updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }

    suspend fun denyJoinRequest(lobbyId: ObjectId, senderId: ObjectId): LobbyDto? {
        // verify lobby exists
        getLobby(lobbyId)

        val updates =
            Updates.combine(
                Updates.pull(LobbyDto::requests.name, eq("_id", senderId))
            )

        return collection.findOneAndUpdate(
            eq("_id", lobbyId), updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }

    suspend fun kickPlayer(lobbyId: ObjectId, kickedId: ObjectId): LobbyDto? {
        // verify lobby exists
        getLobby(lobbyId)

        val updates =
            Updates.combine(
                Updates.pull(LobbyDto::users.name, kickedId)
            )

        return collection.findOneAndUpdate(
            eq("_id", lobbyId), updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }
}