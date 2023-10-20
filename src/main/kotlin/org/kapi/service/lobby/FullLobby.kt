package org.kapi.service.lobby

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.kapi.data.User
import org.kapi.service.user.MinimalUser

@Serializable
data class FullLobby(
    @SerialName("_id")
    @Contextual
    val id: ObjectId? = null,
    val name: String,
    val game: String = "",
    val hostId: @Contextual ObjectId,
    val tags: List<String> = ArrayList(),
    val numPlayers: Int,
    val description: String = "",
    val requests: List<LobbyRequestWithUser> = ArrayList(),
    val users: List<User>,
    @Contextual
    val chat: ObjectId? = null
)

@Serializable
data class LobbyRequestWithUser(
    @SerialName("_id")
    @Contextual
    val id: ObjectId? = null,
    val sender: MinimalUser,
    val message: String? = null
)