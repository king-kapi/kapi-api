package org.kapi.responses

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class InsertedIdResponse(
    @Contextual
    val insertedId: ObjectId
)
