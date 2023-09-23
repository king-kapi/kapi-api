package org.kapi.data

import com.mongodb.client.model.Projections
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.kapi.enums.UserStatus

@Serializable
data class Birthday(
    val day: Int,
    val month: Int,
    val year: Int
)

@Serializable
data class User(
    @SerialName("_id")
    @Contextual val id: ObjectId? = null,
    val email: String,
    val username: String,
    val tag: String,
    val bio: String,
    val status: Int,
    val friends: List<@Contextual ObjectId>? = null,
    val games: List<@Contextual ObjectId>? = null,
    val pronouns: String? = null,
    val birthday: Birthday? = null,
    val language: String? = null,
    val timezone: String? = null,
    val avatarColor: String? = null,
    val lobby: String? = null,
    val onboarded: Boolean
)

@Serializable
data class MinimalUser(
    @SerialName("_id")
    @Contextual val id: ObjectId,
    val username: String,
    val tag: String,
    val bio: String,
    val status: Int,
    val pronouns: String?
)

val minimalUserProjection: Bson =
    Projections.include(User::username.name, User::tag.name, User::bio.name, User::status.name, User::pronouns.name)

@Serializable
data class OnboardingParams(
    val username: String? = null,
    val games: List<@Contextual ObjectId>? = null, // id
    val pronouns: String? = null, // HE_HIM (he-him), SHE_HER (she-her), THEY_THEM (they-them)
    val birthday: Birthday? = null,
    val language: String? = null,
    val timezone: String? = null,
    val avatarColor: String? = null
)

fun createNewUser(email: String) = User(
    email = email,
    username = "",
    tag = "",
    bio = "",
    status = UserStatus.OFFLINE.num,
    onboarded = false
)