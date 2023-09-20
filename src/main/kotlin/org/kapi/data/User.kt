package org.kapi.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Birthday(
    val day: Int,
    val month: Int,
    val year: Int
)

@Serializable
data class User(
    @SerialName("_id")
    @Contextual val id: ObjectId,
    val username: String,
    val tag: String,
    val bio: String,
    val status: Int,
    val friends: List<@Contextual ObjectId>?,
    val games: List<@Contextual ObjectId>?,
    val pronouns: String?,
    val birthday: Birthday?,
    val language: String?,
    val timezone: String?,
    val avatarColor: String?,
    val lobby: String?,
    val onboarded: Boolean
)