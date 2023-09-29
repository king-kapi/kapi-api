package org.kapi.exceptions

import org.bson.types.ObjectId

class UserNotFound(userId: ObjectId) : Exception("Can't find user $userId")