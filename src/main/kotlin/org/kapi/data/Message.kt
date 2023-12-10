package org.kapi.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.bson.types.ObjectId

@Serializable
data class Message(
    @SerialName("_id")
    @Contextual
    val id: ObjectId? = null,
    @Contextual
    val chatId: ObjectId,
    @Contextual
    val senderId: ObjectId,
    val message: String,
    val additional: Bson? = null,
    val timestamp: Long
)
