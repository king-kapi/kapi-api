package org.kapi.service.user

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.kapi.data.User


@Serializable
data class MinimalUser(
    @SerialName("_id")
    @Contextual val id: ObjectId? = null,
    val username: String,
    val tag: String,
    val bio: String,
    val status: Int,
    val pronouns: String? = null,
    val avatarColor: String? = null,
) {
    constructor(user: User) :
            this(user.id, user.username, user.tag, user.bio, user.status, user.pronouns, user.avatarColor)
}