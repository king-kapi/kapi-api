package org.kapi.service.user

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.kapi.data.MinimalUser
import org.kapi.data.OnboardingParams
import org.kapi.data.User
import org.kapi.data.minimalUserProjection
import org.kapi.exceptions.UserNotFound

class UserService(database: MongoDatabase) {
    private val collection = database.getCollection<User>("users")

    suspend fun getAllUsers(): List<User> {
        return collection.find().toList()
    }

    suspend fun getUser(userId: ObjectId): User {
        return collection.find(eq("_id", userId)).firstOrNull() ?: throw UserNotFound(userId)
    }

    suspend fun getUser(userEmail: String): User {
        return collection.find(eq("email", userEmail)).firstOrNull()
            ?: throw Exception("user not found with email $userEmail")
    }

    // TODO: throw the correct exception if friends is null
    suspend fun getFriends(userId: ObjectId): List<MinimalUser> {
        val user = this.getUser(userId)

        if (user.friends == null) {
            throw UserNotFound(userId)
        } else {
            val friends = ArrayList<MinimalUser>()
            for (friendId in user.friends) {
                val friend =
                    collection.find<MinimalUser>(eq("_id", friendId))
                        .projection(minimalUserProjection)
                        .firstOrNull()
                if (friend != null) {
                    friends.add(friend)
                }
            }

            return friends
        }
    }

    suspend fun createNewUser(email: String): User {
        val result = collection.insertOne(org.kapi.data.createNewUser(email))
        val insertedId = result.insertedId?.asObjectId()?.value ?: throw Exception("Something is wrong")
        return this.getUser(insertedId)
    }

    suspend fun onboardUser(
        userId: ObjectId,
        params: OnboardingParams
    ) {
        val update = Updates.combine(
            Updates.set("username", params.username),
            Updates.set("games", params.games),
            Updates.set("pronouns", params.pronouns),
            Updates.set("birthday", params.birthday),
            Updates.set("language", params.language),
            Updates.set("timezone", params.timezone),
            Updates.set("avatarColor", params.avatarColor),
            Updates.set("onboarded", true)
        )

        collection.updateOne(eq("_id", userId), update)
    }
}