package org.kapi.service.chat

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import org.kapi.data.Chat
import org.kapi.exceptions.ChatNotFound

class ChatService(database: MongoDatabase) {
    val collection = database.getCollection<Chat>("chats")

    suspend fun createChat(chat: Chat): Chat {
        val insertedId = collection.insertOne(chat).insertedId!!.asObjectId().value
        return getChat(insertedId)
    }

    suspend fun getChat(chatId: ObjectId): Chat {
        return collection.find(eq("_id", chatId)).firstOrNull() ?: throw ChatNotFound(chatId)
    }

    suspend fun addUser(chatId: ObjectId, userId: ObjectId): Chat {
        val chat = getChat(chatId)
        val update = Updates.push(Chat::users.name, userId);

        return collection.findOneAndUpdate(
            eq("_id", chatId),
            update,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ) ?: throw ChatNotFound(chatId)
    }

    suspend fun removeUser(chatId: ObjectId, userId: ObjectId): Chat {
        val chat = getChat(chatId)
        val update = Updates.pull(Chat::users.name, userId);

        return collection.findOneAndUpdate(
            eq("_id", chatId),
            update,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ) ?: throw ChatNotFound(chatId)
    }

    suspend fun deleteChat(chatId: ObjectId) {
        // verify
        getChat(chatId)

        collection.deleteOne(eq("_id", chatId))
    }
}