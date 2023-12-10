package org.kapi.plugins

import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.bson.types.ObjectId
import org.kapi.chat.ChatConnection
import org.kapi.chat.IncomingMessage
import org.kapi.chat.InitiateMessage
import org.kapi.data.Message
import org.kapi.exceptions.ChatNotFound
import org.kapi.mongo.MongoClientSingleton
import org.kapi.service.chat.ChatService
import org.kapi.service.message.MessageService
import java.util.*

fun Application.configureChatRouting() {
//    val chatIdConnections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    val chatIdConnections = HashMap<ObjectId, Set<ChatConnection>>()
    val chatService = ChatService(MongoClientSingleton.getKapiDatabase(environment))
    val messageService = MessageService(MongoClientSingleton.getKapiDatabase(environment))

    routing {
        webSocket("/api/chat") {
            // manage new connections
            var chatId: ObjectId? = null
            var thisConnection: ChatConnection? = null
            var initiated = false

            try {
                for (frame in incoming) {
                    if (!initiated) {
                        println("Attempting to initiate with user");
                        val initiate = this.converter!!.deserialize<InitiateMessage>(content = frame)

                        chatId = initiate.chatId
                        // verify chat actually exists
                        try {
                            chatService.getChat(chatId)
                        } catch (e: ChatNotFound) {
                            println("chat not found");
                            continue
                        }

                        initiated = true
                        thisConnection = ChatConnection(this, chatId)

                        if (!chatIdConnections.containsKey(chatId))
                            chatIdConnections[chatId] = Collections.synchronizedSet(LinkedHashSet())

                        chatIdConnections[chatId]!!.plus(thisConnection)
                    } else if (chatId != null) {
                        val incomingMessage = this.converter!!.deserialize<IncomingMessage>(content = frame)

                        println("received incoming message ${incomingMessage.message}")

                        val insertedMessage = messageService.createMessage(
                            Message(
                                senderId = incomingMessage.senderId,
                                message = incomingMessage.message,
                                chatId = chatId,
                                timestamp = System.currentTimeMillis()
                            )
                        )

                        sendSerialized(insertedMessage)
                    }
                }
            } finally {
                chatIdConnections[chatId]?.minus(thisConnection)
            }
        }
    }
}