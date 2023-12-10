package org.kapi.chat

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class OutgoingMessage(
    @Contextual
    val senderId: ObjectId,
    val message: String
)
