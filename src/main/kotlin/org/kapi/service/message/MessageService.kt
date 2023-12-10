package org.kapi.service.message

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.kapi.data.Message


class MessageService(database: MongoDatabase) {
    val collection = database.getCollection<Message>("messages")

    suspend fun getMessages(chatId: ObjectId): List<Message> {
        return collection.find(eq("chatId", chatId)).toList()
    }

    suspend fun createMessage(message: Message): Message {
        val insertedId = collection.insertOne(message).insertedId!!.asObjectId().value

        return collection.find(eq("_id", insertedId)).first()
    }

    suspend fun deleteMessage(messageId: ObjectId) {
        collection.deleteOne(eq("_id", messageId))
    }
}