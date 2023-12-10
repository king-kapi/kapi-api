package org.kapi.chat

import io.ktor.websocket.*
import org.bson.types.ObjectId

data class ChatConnection(val session: DefaultWebSocketSession, val chatId: ObjectId)