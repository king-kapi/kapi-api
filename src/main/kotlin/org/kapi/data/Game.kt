package org.kapi.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Game(
    @SerialName("_id")
    @Contextual val id: ObjectId? = null,
    val name: String,
    val image: String,
    val numPlayers: List<Int>
)