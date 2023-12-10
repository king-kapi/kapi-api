package org.kapi.exceptions

import org.bson.types.ObjectId

class ChatNotFound(chatId: ObjectId) : Exception("Can't find chat $chatId")