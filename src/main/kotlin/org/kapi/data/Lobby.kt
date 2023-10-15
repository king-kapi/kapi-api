package org.kapi.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Lobby(
    @SerialName("_id")
    @Contextual
    val id: ObjectId? = null,
    val name: String,
    val game: String = "",
    val hostId: @Contextual ObjectId,
    val tags: List<String> = ArrayList(),
    val numPlayers: Int,
    val description: String = "",
    val requests: List<LobbyRequest> = ArrayList(),
    val users: List<@Contextual ObjectId> = ArrayList(),
    @Contextual
    val chat: ObjectId? = null
)

@Serializable
data class LobbyRequest(
    @SerialName("_id")
    @Contextual
    val id: ObjectId = ObjectId(),
    @Contextual
    val sender: ObjectId,
    val message: String = "",
)