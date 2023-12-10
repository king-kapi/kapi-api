package org.kapi.chat;


import kotlinx.serialization.Contextual;
import kotlinx.serialization.Serializable;
import org.bson.types.ObjectId;

@Serializable
data class InitiateMessage(
    @Contextual
    val chatId: ObjectId
)